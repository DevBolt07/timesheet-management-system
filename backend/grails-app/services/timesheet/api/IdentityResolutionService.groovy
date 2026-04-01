package timesheet.api

import grails.web.api.ServletAttributes

/**
 * Foundation Layer: Identity Context Resolution
 * 
 * This service safely isolates all "current user" lookups.
 * In a real ERP deployment, this is exactly the service you rewrite to parse
 * Spring Security's principal: `springSecurityService.getCurrentUser()`.
 */
class IdentityResolutionService implements ServletAttributes {

    User resolveSessionContext() {
        def request = getRequest()
        
        // --- DEMO / INTEGRATION FALLBACK MODE ---
        // Reads mock explicit User ID simulating a JWT or Server Gateway proxy
        String injectedUserId = request.getHeader("X-ERP-User-Id")
        
        if (!injectedUserId) {
            throw new SecurityException("Unauthorized: Missing ERP Identity Context (JWT/Session Gateway equivalent not found)")
        }

        try {
            Long userId = injectedUserId as Long
            User user = User.get(userId)
            if (!user) {
                throw new SecurityException("Forbidden: Identity Context resolved to a non-existent ERP user instance")
            }
            return user
        } catch (NumberFormatException e) {
            throw new SecurityException("Forbidden: Malformed ERP Identity Context structure")
        }
    }
}
