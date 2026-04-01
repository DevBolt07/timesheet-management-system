package timesheet.api

import grails.converters.JSON
import grails.gorm.transactions.Transactional

@Transactional
class TaskTypeController {

    IdentityResolutionService identityResolutionService
    
    static responseFormats = ['json']

    private User requireAdmin() {
        def user = identityResolutionService.resolveSessionContext()
        if (user.role != Role.ADMIN) {
            throw new SecurityException("Access denied: Admin privileges required")
        }
        return user
    }

    def index() {
        def list = TaskType.where { isActive == true }.list()
        def formattedList = list.collect { t ->
            [ id: t.id, name: t.name ]
        }
        render([success: true, data: formattedList] as JSON)
    }

    def save() {
        try {
            requireAdmin()
            def data = request.JSON
            String name = data.name?.toString()?.trim()
            
            if (!name) {
                render status: 400, text: ([success: false, message: "Task category name cannot be empty"] as JSON)
                return
            }

            def existing = TaskType.findByName(name)
            if (existing) {
                if (!existing.isActive) {
                    existing.isActive = true
                    existing.save(flush: true, failOnError: true)
                    render status: 200, text: ([success: true, data: [id: existing.id, name: existing.name], message: "Reactivated existing task category."] as JSON)
                    return
                }
                render status: 400, text: ([success: false, message: "Task category already exists and is active."] as JSON)
                return
            }

            def task = new TaskType(name: name)
            task.save(flush: true, failOnError: true)
            render status: 201, text: ([success: true, data: [id: task.id, name: task.name], message: "Task category created safely."] as JSON)

        } catch (SecurityException e) {
            render status: 403, text: ([success: false, message: e.message] as JSON)
        } catch (Exception e) {
            render status: 500, text: ([success: false, message: e.message] as JSON)
        }
    }

    def delete(Long id) {
        try {
            requireAdmin()
            def task = TaskType.get(id)
            if (!task) {
                render status: 404, text: ([success: false, message: "Task category not found"] as JSON)
                return
            }
            
            int usageCount = Timesheet.countByTaskType(task)
            if (usageCount > 0) {
                // Soft delete to preserve historical integrity
                task.isActive = false
                task.save(flush: true, failOnError: true)
                render status: 200, text: ([success: true, message: "Task category deactivated (preserved in history)."] as JSON)
            } else {
                // Hard delete if purely completely unused
                task.delete(flush: true)
                render status: 200, text: ([success: true, message: "Task category safely deleted from master."] as JSON)
            }
        } catch (SecurityException e) {
            render status: 403, text: ([success: false, message: e.message] as JSON)
        } catch (Exception e) {
            render status: 500, text: ([success: false, message: e.message] as JSON)
        }
    }
}
