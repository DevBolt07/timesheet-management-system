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
        if (!start.isBefore(end)) {
            throw new IllegalArgumentException("End time must be after start time")
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
