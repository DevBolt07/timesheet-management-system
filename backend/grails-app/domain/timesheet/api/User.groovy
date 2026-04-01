package timesheet.api

import java.security.MessageDigest

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

    // A lightweight hash utility safely substituting for Spring Security during the integration demo phase
    static String hashString(String input) {
        def md = MessageDigest.getInstance("SHA-256")
        md.update(input.getBytes("UTF-8"))
        return md.digest().encodeHex().toString()
    }
}

enum Role {
    STAFF, HOD, ADMIN
}
