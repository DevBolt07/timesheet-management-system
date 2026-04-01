package timesheet.api

class UrlMappings {
    static mappings = {
        "/$controller/$action?/$id?(.$format)?"{
            constraints {
                // apply constraints here
            }
        }
        
        "/api/timesheets/$id/review"(controller: 'timesheet', action: 'review', method: 'PUT')
        "/api/timesheets"(resources: 'timesheet')
        "/api/taskTypes"(controller: 'taskType', action: 'index', method: 'GET')
        "/api/auth/demo-login"(controller: 'auth', action: 'demoLogin', method: 'POST')
        "/api/users/staff"(controller: 'timesheet', action: 'staffList', method: 'GET')

        // Admin routes — ADMIN role only
        "/api/admin/metrics"(controller: 'admin', action: 'metrics', method: 'GET')
        "/api/admin/timesheets"(controller: 'admin', action: 'timesheets', method: 'GET')
        "/api/admin/timesheets/$id/reopen"(controller: 'admin', action: 'reopenTimesheet', method: 'PUT')
        "/api/admin/users"(controller: 'admin', action: 'users', method: 'GET')
        "/api/admin/tasks"(controller: 'admin', action: 'tasks', method: 'GET')
        "/api/admin/tasks"(controller: 'admin', action: 'createTask', method: 'POST')
        "/api/admin/tasks/$id"(controller: 'admin', action: 'deleteTask', method: 'DELETE')

        "/"(view:"/index")
        "500"(view:'/error')
        "404"(view:'/notFound')

    }
}
