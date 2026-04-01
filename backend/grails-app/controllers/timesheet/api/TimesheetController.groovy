package timesheet.api

import grails.converters.JSON
import grails.gorm.transactions.Transactional

@Transactional
class TimesheetController {

    TimesheetService timesheetService
    IdentityResolutionService identityResolutionService
    
    static responseFormats = ['json']

    private User getAuthenticatedUser() {
        return identityResolutionService.resolveSessionContext()
    }

    private Map buildTimesheetDTO(Timesheet t) {
        return [
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

    // Returns list of staff users for HOD filtering dropdowns
    def staffList() {
        try {
            User currentUser = getAuthenticatedUser()
            if (currentUser.role != Role.HOD && currentUser.role != Role.ADMIN) {
                render status: 403, text: ([success: false, message: 'Access restricted to management roles'] as JSON)
                return
            }
            def staffUsers = User.findAllByRole(Role.STAFF).collect { u ->
                [id: u.id, username: u.username]
            }
            render([success: true, data: staffUsers] as JSON)
        } catch (SecurityException e) {
            render status: 403, text: ([success: false, message: e.message] as JSON)
        } catch (Exception e) {
            render status: 500, text: ([success: false, message: e.message] as JSON)
        }
    }

    def index() {
        try {
            User currentUser = getAuthenticatedUser()
            def list = timesheetService.getAllTimesheets(currentUser)
            def formattedList = list.collect { buildTimesheetDTO(it) }
            render([success: true, data: formattedList] as JSON)
        } catch (SecurityException e) {
            render status: 403, text: ([success: false, message: e.message] as JSON)
        } catch (Exception e) {
            render status: 500, text: ([success: false, message: e.message] as JSON)
        }
    }

    def show(Long id) {
        try {
            User currentUser = getAuthenticatedUser()
            def t = timesheetService.getTimesheet(id, currentUser)
            if (!t) {
                render status: 404, text: ([success: false, message: "Timesheet not found or unauthorized"] as JSON)
                return
            }
            render([success: true, data: buildTimesheetDTO(t)] as JSON)
        } catch (SecurityException e) {
            render status: 403, text: ([success: false, message: e.message] as JSON)
        } catch (Exception e) {
            render status: 500, text: ([success: false, message: e.message] as JSON)
        }
    }

    def save() {
        try {
            User currentUser = getAuthenticatedUser()
            def data = request.JSON
            
            String taskName = data.task as String
            def taskType = TaskType.findByName(taskName) ?: new TaskType(name: taskName).save(flush:true, failOnError:true)

            def timesheet = timesheetService.createTimesheet(data, currentUser, taskType)
            
            render status: 201, text: ([success: true, data: buildTimesheetDTO(timesheet), message: 'Log added successfully'] as JSON)

        } catch (SecurityException e) {
            render status: 403, text: ([success: false, message: e.message] as JSON)
        } catch (IllegalArgumentException e) {
            render status: 400, text: ([success: false, message: e.message] as JSON)
        } catch (Exception e) {
            render status: 500, text: ([success: false, message: e.message] as JSON)
        }
    }

    def update(Long id) {
        try {
            User currentUser = getAuthenticatedUser()
            def data = request.JSON
            
            TaskType taskType = null
            if (data.task) {
                String taskName = data.task as String
                taskType = TaskType.findByName(taskName) ?: new TaskType(name: taskName).save(flush:true, failOnError:true)
            }

            def timesheet = timesheetService.updateTimesheet(id, data, taskType, currentUser)
            render([success: true, data: buildTimesheetDTO(timesheet), message: 'Updated successfully'] as JSON)

        } catch (SecurityException e) {
            render status: 403, text: ([success: false, message: e.message] as JSON)
        } catch (IllegalArgumentException e) {
            render status: 400, text: ([success: false, message: e.message] as JSON)
        } catch (Exception e) {
            render status: 500, text: ([success: false, message: e.message] as JSON)
        }
    }

    def delete(Long id) {
        try {
            User currentUser = getAuthenticatedUser()
            timesheetService.deleteTimesheet(id, currentUser)
            render([success: true, message: 'Deleted successfully'] as JSON)
        } catch (SecurityException e) {
            render status: 403, text: ([success: false, message: e.message] as JSON)
        } catch (IllegalArgumentException e) {
            render status: 400, text: ([success: false, message: e.message] as JSON)
        } catch (Exception e) {
            render status: 500, text: ([success: false, message: e.message] as JSON)
        }
    }

    def review(Long id) {
        try {
            User currentUser = getAuthenticatedUser()
            def data = request.JSON
            
            if (!data.status) {
                render status: 400, text: ([success: false, message: 'Status is required for review workflow'] as JSON)
                return
            }

            Status newStatus = Status.valueOf(data.status as String)
            def timesheet = timesheetService.reviewTimesheet(id, newStatus, data.remarks as String, currentUser)
            
            render status: 200, text: ([success: true, data: buildTimesheetDTO(timesheet), message: 'Review successfully saved'] as JSON)
            
        } catch (SecurityException e) {
            render status: 403, text: ([success: false, message: e.message] as JSON)
        } catch (IllegalArgumentException e) {
            render status: 400, text: ([success: false, message: e.message] as JSON)
        } catch (Exception e) {
            render status: 500, text: ([success: false, message: e.message] as JSON)
        }
    }
}
