package timesheet.api

class User {
    String username
    String passwordHash
    Role role

    static hasMany = [timesheets: Timesheet]

    static constraints = {
        username blank: false, unique: true
        passwordHash blank: false
        role nullable: false
    }
}

enum Role {
    STAFF, HOD
}
