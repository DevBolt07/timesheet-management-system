package timesheet.api

import grails.converters.JSON

class TimesheetController {

    TimesheetService timesheetService
    
    static responseFormats = ['json']

    def index() {
        def list = timesheetService.getAllTimesheets()
        
        // Custom formatting array for the exact UI schema expectation
        def formattedList = list.collect { t ->
            [
                id: t.id,
                date: t.entryDate.toString(),
                startTime: t.startTime.toString(),
                endTime: t.endTime.toString(),
                task: t.taskType?.name,
                description: t.description,
                status: t.status.name()
            ]
        }
        
        render([success: true, data: formattedList] as JSON)
    }

    def show(Long id) {
        def t = timesheetService.getTimesheet(id)
        if (!t) {
            render status: 404, text: ([success: false, message: "Not found"] as JSON)
            return
        }
        
        def dict = [
            id: t.id,
            date: t.entryDate.toString(),
            startTime: t.startTime.toString(),
            endTime: t.endTime.toString(),
            task: t.taskType?.name,
            description: t.description,
            status: t.status.name()
        ]
        render([success: true, data: dict] as JSON)
    }

    def save() {
        try {
            def data = request.JSON
            
            // Mocking a User and TaskType dynamically since no auth/DB seed exists yet
            def user = User.first() ?: new User(username: 'staff_user', passwordHash: 'hash', role: Role.STAFF).save(flush:true, failOnError:true)
            
            String taskName = data.task as String
            def taskType = TaskType.findByName(taskName) ?: new TaskType(name: taskName).save(flush:true, failOnError:true)

            def timesheet = timesheetService.createTimesheet(data, user, taskType)
            
            def result = [
                id: timesheet.id,
                date: timesheet.entryDate.toString(),
                startTime: timesheet.startTime.toString(),
                endTime: timesheet.endTime.toString(),
                task: timesheet.taskType?.name,
                description: timesheet.description,
                status: timesheet.status.name()
            ]
            
            render status: 201, text: ([success: true, data: result, message: 'Log added successfully'] as JSON)

        } catch (IllegalArgumentException e) {
            render status: 400, text: ([success: false, message: e.message] as JSON)
        } catch (Exception e) {
            render status: 500, text: ([success: false, message: e.message] as JSON)
        }
    }

    def update(Long id) {
        try {
            def data = request.JSON
            
            TaskType taskType = null
            if (data.task) {
                String taskName = data.task as String
                taskType = TaskType.findByName(taskName) ?: new TaskType(name: taskName).save(flush:true, failOnError:true)
            }

            def timesheet = timesheetService.updateTimesheet(id, data, taskType)
            render([success: true, data: timesheet, message: 'Updated successfully'] as JSON)

        } catch (IllegalArgumentException e) {
            render status: 400, text: ([success: false, message: e.message] as JSON)
        } catch (Exception e) {
            render status: 500, text: ([success: false, message: e.message] as JSON)
        }
    }

    def delete(Long id) {
        try {
            timesheetService.deleteTimesheet(id)
            render([success: true, message: 'Deleted successfully'] as JSON)
        } catch (IllegalArgumentException e) {
            render status: 400, text: ([success: false, message: e.message] as JSON)
        } catch (Exception e) {
            render status: 500, text: ([success: false, message: e.message] as JSON)
        }
    }
}
