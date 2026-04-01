package timesheet.api

import grails.converters.JSON

class AuthController {

    static responseFormats = ['json']

    /**
     * Foundation Layer: Identity Context Resolver
     * Mocks a real authentication endpoint mapping explicitly to BootStrap DB identities.
     * Prevents the frontend from dangerously guessing autoincremented ID numbers.
     * In an ERP, this would validate claims and generate a JWT token struct.
     */
    def demoLogin() {
        try {
            def data = request.JSON
            String username = data.username
            String password = data.password

            if (!username || !password) {
                render status: 400, text: ([success: false, message: "Both username and password are required for securely starting an integration simulated session"] as JSON)
                return
            }

            def user = User.findByUsername(username)
            if (!user || user.passwordHash != User.hashString(password)) {
                render status: 401, text: ([success: false, message: "Invalid credentials provided"] as JSON)
                return
            }

            // Provide realistic visual names mapped dynamically to the seeded identities
            String displayName = "Unrecognized Staff"
            if (user.username == 'dr.sharma') displayName = "Dr. Sharma"
            if (user.username == 'dipak.pawar') displayName = "Dipak Pawar"
            if (user.username == 'kavita.deshmukh') displayName = "Kavita Deshmukh"
            if (user.username == 'rahul.verma') displayName = "Rahul Verma"
            if (user.username == 'system.admin') displayName = "System Administrator"

            def contextPayload = [
                id: user.id,
                username: user.username,
                role: user.role.name(),
                name: displayName,
                department: 'Computer Engineering' // Placeholder org struct
            ]

            render status: 200, text: ([success: true, data: contextPayload, message: 'Identity reliably resolved'] as JSON)

        } catch (Exception e) {
            render status: 500, text: ([success: false, message: e.message] as JSON)
        }
    }
}
