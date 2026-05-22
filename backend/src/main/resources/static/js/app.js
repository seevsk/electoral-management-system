// ============================================
// Sistema Electoral - App JavaScript
// ============================================

// Alpine.js auth store
document.addEventListener('alpine:init', () => {
    Alpine.store('auth', {
        authenticated: localStorage.getItem('auth') !== null,
        user: JSON.parse(localStorage.getItem('auth')),

        login(role) {
            const user = {
                role: role,
                name: role === 'admin' ? 'Admin' : 'Votante',
            }
            localStorage.setItem('auth', JSON.stringify(user))
            this.authenticated = true
            this.user = user
            const base = typeof CTX !== 'undefined' ? CTX : '/'
            if (role === 'admin') {
                window.location.href = base + 'menuprincipal'
            } else {
                window.location.href = base
            }
        },

        logout() {
            localStorage.removeItem('auth')
            this.authenticated = false
            this.user = null
            const base = typeof CTX !== 'undefined' ? CTX : '/'
            window.location.href = base
        }
    })
})

document.addEventListener('DOMContentLoaded', function () {
    // Inicializar iconos Lucide
    if (typeof lucide !== 'undefined') {
        lucide.createIcons();
    }
});
