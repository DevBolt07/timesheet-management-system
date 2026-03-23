package timesheet.api

class UrlMappings {
    static mappings = {
        "/$controller/$action?/$id?(.$format)?"{
            constraints {
                // apply constraints here
            }
        }
        
        "/api/timesheets"(resources: 'timesheet')

        "/"(view:"/index")
        "500"(view:'/error')
        "404"(view:'/notFound')

    }
}
