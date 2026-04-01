package timesheet.api

import grails.gorm.transactions.Transactional
import java.time.LocalDate
import java.time.LocalTime
import java.time.LocalDateTime

@Transactional
class TimesheetService {

    def getAllTimesheets(User currentUser) {
        // Real-world integration: HOD would see `Timesheet.list` scoped by their Department property
        if (currentUser.role == Role.HOD) {
            return Timesheet.list(sort: 'entryDate', order: 'desc')
        }
        
        // Staff only see their inherently owned modules mapped implicitly 
        return Timesheet.findAllByUser(currentUser, [sort: 'entryDate', order: 'desc'])
    }

    def getTimesheet(Long id, User currentUser) {
        def t = Timesheet.get(id)
        if (currentUser.role != Role.HOD && t?.user?.id != currentUser.id) {
            return null // unauthorized to view another user's timesheets unless Manager
        }
        return t
    }

    def createTimesheet(Map data, User currentUser, TaskType taskType, String taskName) {
        if (currentUser.role != Role.STAFF) throw new SecurityException("Only staff roles can actively log timesheets")
        
        if (!data.date) throw new IllegalArgumentException("Operational date is required")
        if (!data.startTime || !data.endTime) throw new IllegalArgumentException("Start and end times are required")
        if (!data.description?.trim()) throw new IllegalArgumentException("A brief description is required")
        if (!taskType && !data.task?.trim()) throw new IllegalArgumentException("Task categorization is required")

        if (!data.startTime.matches('^([01]\\d|2[0-3]):([0-5]\\d):([0-5]\\d)$')) throw new IllegalArgumentException("Invalid Start Time structural bounds")
        if (!data.endTime.matches('^([01]\\d|2[0-3]):([0-5]\\d):([0-5]\\d)$')) throw new IllegalArgumentException("Invalid End Time structural bounds")

        LocalDate date = LocalDate.parse(data.date as String)
        LocalTime start = LocalTime.parse(data.startTime as String)
        LocalTime end = LocalTime.parse(data.endTime as String)

        validateTimesheetData(date, start, end, currentUser, null)

        def timesheet = new Timesheet(
            user: currentUser,
            taskType: taskType,
            customTaskName: taskType ? null : taskName?.trim(),
            entryDate: date,
            startTime: start,
            endTime: end,
            description: data.description as String,
            status: Status.PENDING
        )

        timesheet.save(flush: true, failOnError: true)
        return timesheet
    }

    def updateTimesheet(Long id, Map data, TaskType taskType, String taskName, User currentUser) {
        if (currentUser.role != Role.STAFF) throw new SecurityException("Only staff users can modify timesheet states")
        
        Timesheet timesheet = Timesheet.get(id)
        if (!timesheet) throw new IllegalArgumentException("Timesheet instance target could not be successfully resolved")
        if (timesheet.user.id != currentUser.id) throw new SecurityException("You do not hold modifying ownership of this target record")
        if (timesheet.status == Status.APPROVED) throw new IllegalArgumentException("Approved timesheets are locked and cannot be modified.")

        LocalDate date = data.date ? LocalDate.parse(data.date as String) : timesheet.entryDate
        LocalTime start = data.startTime ? LocalTime.parse(data.startTime as String) : timesheet.startTime
        LocalTime end = data.endTime ? LocalTime.parse(data.endTime as String) : timesheet.endTime

        validateTimesheetData(date, start, end, timesheet.user, timesheet.id)

        timesheet.entryDate = date
        timesheet.startTime = start
        timesheet.endTime = end
        timesheet.description = data.description != null ? data.description as String : timesheet.description
        
        if (data.task != null) {
            timesheet.taskType = taskType
            timesheet.customTaskName = taskType ? null : taskName?.trim()
        }

        // Rejected entries become pending again once the staff member resubmits changes.
        if (timesheet.status == Status.REJECTED) {
            timesheet.status = Status.PENDING
            timesheet.reviewerRemarks = null
        }

        timesheet.save(flush: true, failOnError: true)
        return timesheet
    }

    def deleteTimesheet(Long id, User currentUser) {
        if (currentUser.role != Role.STAFF) throw new SecurityException("Managers do not uniquely possess structural permissions to explicitly delete target entries")
        
        Timesheet timesheet = Timesheet.get(id)
        if (!timesheet) throw new IllegalArgumentException("Timesheet instance target could not be successfully resolved")
        if (timesheet.user.id != currentUser.id) throw new SecurityException("Cannot command deletions upon outside user boundaries internally")
        if (timesheet.status != Status.PENDING) throw new IllegalArgumentException("Only universally PENDING models can undergo successful deletion mappings.")
        if (timesheet.adminOverrideAt) throw new IllegalArgumentException("This entry was reopened by Admin for correction. Please update and resubmit it instead of deleting it.")
        
        timesheet.delete(flush: true)
    }

    def reviewTimesheet(Long id, Status newStatus, String remarks, User currentUser) {
        if (currentUser.role != Role.HOD) throw new SecurityException("Only active HOD management roles explicitly possess review approval rights")
        
        Timesheet timesheet = Timesheet.get(id)
        if (!timesheet) throw new IllegalArgumentException("Target configuration log missing from relational domain")
        if (timesheet.status != Status.PENDING) throw new IllegalArgumentException("Configuration reviews can solely occur against PENDING transitions")
        if (newStatus == Status.PENDING) throw new IllegalArgumentException("You inherently cannot map a workflow recursively onto its PENDING origin state")

        timesheet.status = newStatus
        timesheet.reviewerRemarks = remarks?.trim() ?: null
        timesheet.save(flush: true, failOnError: true)
        return timesheet
    }

    def adminReopenTimesheet(Long id, String reason, User adminUser) {
        if (adminUser.role != Role.ADMIN) {
            throw new SecurityException("Only Admin can reopen locked workflow records")
        }

        String trimmedReason = reason?.trim()
        if (!trimmedReason) {
            throw new IllegalArgumentException("Admin override reason is required")
        }

        Timesheet timesheet = Timesheet.get(id)
        if (!timesheet) {
            throw new IllegalArgumentException("Target timesheet could not be resolved")
        }
        if (timesheet.status == Status.PENDING) {
            throw new IllegalArgumentException("Pending entries do not require admin reopening")
        }

        timesheet.adminOverrideFromStatus = timesheet.status
        timesheet.adminOverrideReason = trimmedReason
        timesheet.adminOverrideBy = adminUser.username
        timesheet.adminOverrideAt = LocalDateTime.now()
        timesheet.status = Status.PENDING
        timesheet.save(flush: true, failOnError: true)
        return timesheet
    }

    private void validateTimesheetData(LocalDate date, LocalTime start, LocalTime end, User currentUser, Long excludeId) {
        if (date.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Impossible to systematically bind task instances purely attached to unrecognized future dates intrinsically")
        }
        
        if (start.equals(end)) {
            throw new IllegalArgumentException("Temporal structures dictating an explicitly zero interval duration map are invalid")
        }
        
        if (!start.isBefore(end)) {
            throw new IllegalArgumentException("End timings algorithmically must trail explicit Start times chronologically strictly")
        }

        // Fetch all logically recognized (non-cancelled) timesheets checking explicit overlap boundaries mapped by user identifier exactly
        def existingEntries = Timesheet.where {
            user == currentUser
            entryDate == date
            status != Status.REJECTED // Omitted because rejected items mathematically void scheduling restrictions
        }.list()

        for (Timesheet existing in existingEntries) {
            if (excludeId && existing.id == excludeId) continue

            if (start.isBefore(existing.endTime) && end.isAfter(existing.startTime)) {
                throw new IllegalArgumentException("System logic isolated temporal overlap bounding task block functionally: ${existing.taskType?.name ?: 'Task context'} (${existing.startTime} - ${existing.endTime})")
            }
        }
    }
}
