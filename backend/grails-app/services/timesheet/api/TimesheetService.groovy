package timesheet.api

import grails.gorm.transactions.Transactional
import java.time.LocalDate
import java.time.LocalTime

@Transactional
class TimesheetService {

    def getAllTimesheets() {
        return Timesheet.list(sort: 'entryDate', order: 'desc')
    }

    def getTimesheet(Long id) {
        return Timesheet.get(id)
    }

    def createTimesheet(Map data, User user, TaskType taskType) {
        if (!data.date) throw new IllegalArgumentException("Operational date is required")
        if (!data.startTime || !data.endTime) throw new IllegalArgumentException("Start and end times are required")
        if (!data.description?.trim()) throw new IllegalArgumentException("A brief description is required")
        if (!taskType && !data.task?.trim()) throw new IllegalArgumentException("Task categorization is required")

        if (!data.startTime.matches('^([01]\\d|2[0-3]):([0-5]\\d):([0-5]\\d)$')) throw new IllegalArgumentException("Invalid Start Time bounds structure")
        if (!data.endTime.matches('^([01]\\d|2[0-3]):([0-5]\\d):([0-5]\\d)$')) throw new IllegalArgumentException("Invalid End Time bounds structure")

        LocalDate date = LocalDate.parse(data.date as String)
        LocalTime start = LocalTime.parse(data.startTime as String)
        LocalTime end = LocalTime.parse(data.endTime as String)

        validateTimesheetData(date, start, end, user, null)

        def timesheet = new Timesheet(
            user: user,
            taskType: taskType,
            entryDate: date,
            startTime: start,
            endTime: end,
            description: data.description as String,
            status: Status.PENDING
        )

        timesheet.save(flush: true, failOnError: true)
        return timesheet
    }

    def updateTimesheet(Long id, Map data, TaskType taskType) {
        Timesheet timesheet = Timesheet.get(id)
        if (!timesheet) throw new IllegalArgumentException("Timesheet not found")
        if (timesheet.status != Status.PENDING) throw new IllegalArgumentException("Only PENDING timesheets can be updated")

        LocalDate date = data.date ? LocalDate.parse(data.date as String) : timesheet.entryDate
        LocalTime start = data.startTime ? LocalTime.parse(data.startTime as String) : timesheet.startTime
        LocalTime end = data.endTime ? LocalTime.parse(data.endTime as String) : timesheet.endTime

        validateTimesheetData(date, start, end, timesheet.user, timesheet.id)

        timesheet.entryDate = date
        timesheet.startTime = start
        timesheet.endTime = end
        timesheet.description = data.description != null ? data.description as String : timesheet.description
        
        if (taskType) {
            timesheet.taskType = taskType
        }

        timesheet.save(flush: true, failOnError: true)
        return timesheet
    }

    def deleteTimesheet(Long id) {
        Timesheet timesheet = Timesheet.get(id)
        if (!timesheet) throw new IllegalArgumentException("Timesheet not found")
        if (timesheet.status != Status.PENDING) throw new IllegalArgumentException("Only PENDING timesheets can be deleted")
        
        timesheet.delete(flush: true)
    }

    private void validateTimesheetData(LocalDate date, LocalTime start, LocalTime end, User user, Long excludeId) {
        if (date.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Cannot log timesheet for a future date")
        }
        
        if (start.equals(end)) {
            throw new IllegalArgumentException("Start time and end time cannot be equal")
        }
        
        if (!start.isBefore(end)) {
            throw new IllegalArgumentException("End time must be strictly after start time")
        }

        // Fetch all existing timesheets for this user on this exact date
        def existingEntries = Timesheet.where {
            user == user
            entryDate == date
        }.list()

        for (Timesheet existing in existingEntries) {
            if (excludeId && existing.id == excludeId) continue

            // The absolute overlap formula: start < existing.end AND end > existing.start
            if (start.isBefore(existing.endTime) && end.isAfter(existing.startTime)) {
                throw new IllegalArgumentException("Time overlaps with existing task: ${existing.taskType.name} (${existing.startTime} - ${existing.endTime})")
            }
        }
    }
}
