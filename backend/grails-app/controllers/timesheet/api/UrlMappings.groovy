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
        "/api/taskTypes"(resources: 'taskType')
        "/api/auth/demo-login"(controller: 'auth', action: 'demoLogin', method: 'POST')

        "/"(view:"/index")
        "500"(view:'/error')
        "404"(view:'/notFound')

    }
}
