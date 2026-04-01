package timesheet.api

class BootStrap {

    def init = { servletContext ->
        def masterTasks = [
            "Lecture / Teaching",
            "Lab Session",
            "Student Mentoring",
            "Internal Meeting",
            "Department Administration",
            "Examination Duty",
            "Paper Evaluation",
            "Research Work",
            "Documentation / Reporting",
            "Training / Workshop",
            "Curriculum Planning",
            "Placement / Coordination"
        ]

        TaskType.withTransaction {
            masterTasks.each { taskName ->
                if (!TaskType.findByName(taskName)) {
                    new TaskType(name: taskName).save(failOnError: true)
                }
            }
            
            // Stable Demo Identity Upsert Seeding
            // Ensures repeated environment runs continuously overwrite stale historical password hashes
            def seedUser = { String uName, String unhashedPwd, Role uRole ->
                def u = User.findByUsername(uName) ?: new User(username: uName)
                u.passwordHash = User.hashString(unhashedPwd)
                u.role = uRole
                u.save(failOnError: true)
            }

            seedUser('dipak.pawar', 'staff123', Role.STAFF)
            seedUser('kavita.deshmukh', 'staff123', Role.STAFF)
            seedUser('rahul.verma', 'staff123', Role.STAFF)
            seedUser('dr.sharma', 'manager123', Role.HOD)
            seedUser('system.admin', 'admin123', Role.ADMIN)
        }
    }
    def destroy = {
    }
}
