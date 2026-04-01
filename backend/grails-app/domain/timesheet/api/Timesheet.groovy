package timesheet.api

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class Timesheet {
    LocalDate entryDate
    LocalTime startTime
    LocalTime endTime
    String customTaskName
    String description
    String reviewerRemarks
    String adminOverrideReason
    String adminOverrideBy
    Status adminOverrideFromStatus
    LocalDateTime adminOverrideAt
    Status status = Status.PENDING

    User user
    TaskType taskType

    static belongsTo = [user: User]

    static constraints = {
        entryDate nullable: false
        startTime nullable: false
        endTime nullable: false
        taskType nullable: true
        customTaskName nullable: true, blank: false
        description nullable: true
        reviewerRemarks nullable: true
        adminOverrideReason nullable: true, maxSize: 1000
        adminOverrideBy nullable: true
        adminOverrideFromStatus nullable: true
        adminOverrideAt nullable: true
        status nullable: false
    }
}

enum Status {
    PENDING, APPROVED, REJECTED
}
