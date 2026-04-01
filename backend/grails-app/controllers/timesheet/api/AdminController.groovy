package timesheet.api

import grails.converters.JSON
import grails.gorm.transactions.Transactional

@Transactional
class AdminController {

    IdentityResolutionService identityResolutionService

    static responseFormats = ['json']

    private User requireAdmin() {
        def user = identityResolutionService.resolveSessionContext()
        if (user.role != Role.ADMIN) {
            throw new SecurityException("Access denied: Admin privileges required")
        }
        return user
    }

    // ── Dashboard Metrics ────────────────────────────────────────
    // GET /api/admin/metrics
    def metrics() {
        try {
            requireAdmin()

            def totalTimesheets  = Timesheet.count()
            def pendingCount     = Timesheet.countByStatus(Status.PENDING)
            def approvedCount    = Timesheet.countByStatus(Status.APPROVED)
            def rejectedCount    = Timesheet.countByStatus(Status.REJECTED)
            def staffCount       = User.countByRole(Role.STAFF)
            def hodCount         = User.countByRole(Role.HOD)
            def adminCount       = User.countByRole(Role.ADMIN)
            def taskCount        = TaskType.count()

            render([success: true, data: [
                timesheets: [total: totalTimesheets, pending: pendingCount, approved: approvedCount, rejected: rejectedCount],
                users:      [staff: staffCount, hod: hodCount, admin: adminCount],
                tasks:      [total: taskCount]
            ]] as JSON)

        } catch (SecurityException e) {
            render status: 403, text: ([success: false, message: e.message] as JSON)
        } catch (Exception e) {
            render status: 500, text: ([success: false, message: e.message] as JSON)
        }
    }

    // ── Full Timesheet Oversight ─────────────────────────────────
    // GET /api/admin/timesheets
    def timesheets() {
        try {
            requireAdmin()

            def list = Timesheet.list(sort: 'entryDate', order: 'desc').collect { t ->
                [
                    id: t.id,
                    date: t.entryDate.toString(),
                    startTime: t.startTime.toString(),
                    endTime: t.endTime.toString(),
                    task: t.taskType?.name,
                    description: t.description,
                    status: t.status.name(),
                    reviewerRemarks: t.reviewerRemarks,
                    user: t.user?.username,
                    userId: t.user?.id
                ]
            }

            render([success: true, data: list] as JSON)

        } catch (SecurityException e) {
            render status: 403, text: ([success: false, message: e.message] as JSON)
        } catch (Exception e) {
            render status: 500, text: ([success: false, message: e.message] as JSON)
        }
    }

    // ── User Listing ─────────────────────────────────────────────
    // GET /api/admin/users
    def users() {
        try {
            requireAdmin()

            def list = User.list(sort: 'role').collect { u ->
                [
                    id: u.id,
                    username: u.username,
                    role: u.role.name(),
                    department: 'Computer Engineering' // placeholder until a department field is added to User
                ]
            }

            render([success: true, data: list] as JSON)

        } catch (SecurityException e) {
            render status: 403, text: ([success: false, message: e.message] as JSON)
        } catch (Exception e) {
            render status: 500, text: ([success: false, message: e.message] as JSON)
        }
    }

    // ── Task Master Control ──────────────────────────────────────
    // GET /api/admin/tasks
    def tasks() {
        try {
            requireAdmin()
            def list = TaskType.list().collect { t ->
                def usageCount = Timesheet.countByTaskType(t)
                [id: t.id, name: t.name, usageCount: usageCount]
            }
            render([success: true, data: list] as JSON)
        } catch (SecurityException e) {
            render status: 403, text: ([success: false, message: e.message] as JSON)
        } catch (Exception e) {
            render status: 500, text: ([success: false, message: e.message] as JSON)
        }
    }

    // POST /api/admin/tasks
    def createTask() {
        try {
            requireAdmin()
            def data = request.JSON
            String name = data.name?.trim()
            if (!name) {
                render status: 400, text: ([success: false, message: 'Task name is required'] as JSON)
                return
            }
            if (TaskType.findByName(name)) {
                render status: 409, text: ([success: false, message: 'Task category already exists'] as JSON)
                return
            }
            def task = new TaskType(name: name).save(flush: true, failOnError: true)
            render status: 201, text: ([success: true, data: [id: task.id, name: task.name, usageCount: 0]] as JSON)
        } catch (SecurityException e) {
            render status: 403, text: ([success: false, message: e.message] as JSON)
        } catch (Exception e) {
            render status: 500, text: ([success: false, message: e.message] as JSON)
        }
    }

    // DELETE /api/admin/tasks/$id
    def deleteTask(Long id) {
        try {
            requireAdmin()
            def task = TaskType.get(id)
            if (!task) {
                render status: 404, text: ([success: false, message: 'Task category not found'] as JSON)
                return
            }
            def usageCount = Timesheet.countByTaskType(task)
            if (usageCount > 0) {
                render status: 409, text: ([success: false, message: "Cannot delete '${task.name}': it is referenced by ${usageCount} timesheet record(s). Deactivation support is planned for a future release."] as JSON)
                return
            }
            task.delete(flush: true)
            render([success: true, message: 'Task category deleted'] as JSON)
        } catch (SecurityException e) {
            render status: 403, text: ([success: false, message: e.message] as JSON)
        } catch (Exception e) {
            render status: 500, text: ([success: false, message: e.message] as JSON)
        }
    }
}
