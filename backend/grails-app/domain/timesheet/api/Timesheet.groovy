package timesheet.api

import java.time.LocalDate
import java.time.LocalTime

class Timesheet {
    LocalDate entryDate
    LocalTime startTime
    LocalTime endTime
    String description
    Status status = Status.PENDING

    static belongsTo = [user: User, taskType: TaskType]

    static constraints = {
        entryDate nullable: false
        startTime nullable: false
        endTime nullable: false
        description nullable: true
        status nullable: false
    }
}

enum Status {
    PENDING, APPROVED, REJECTED
}
