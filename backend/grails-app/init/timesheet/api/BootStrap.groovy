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
        }
    }
    def destroy = {
    }
}
