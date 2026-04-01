package timesheet.api

import groovy.sql.Sql

class BootStrap {

    def dataSource

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

            alignOptionalTaskReference()
        }
    }
    def destroy = {
    }

    private void alignOptionalTaskReference() {
        Sql sql = new Sql(dataSource)
        try {
            sql.execute('ALTER TABLE timesheet MODIFY COLUMN task_type_id BIGINT NULL')
        } catch (Exception ignored) {
            // Ignore if the schema is already aligned or the table is not yet available during bootstrap.
        } finally {
            sql.close()
        }
    }
}
