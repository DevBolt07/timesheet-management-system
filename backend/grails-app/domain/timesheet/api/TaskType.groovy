package timesheet.api

class TaskType {
    String name
    Boolean isActive = true

    static constraints = {
        name blank: false, unique: true
    }
}
