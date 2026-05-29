/**
 * sidebar.js — Sidebar injector & manager
 *
 * Usage: Add to any dashboard page (after /js/api.js):
 *
 *   <div id="sidebar-root"></div>
 *   <script src="/js/sidebar.js"></script>
 *
 * The script:
 *  1. Fetches /sidebar.html and injects it into #sidebar-root
 *  2. Wraps the existing page body in .app-layout / .app-main
 *  3. Marks the active nav item based on current pathname
 *  4. Filters nav items by the current user's role
 *  5. Restores collapsed state from localStorage
 *  6. Wires up the collapse toggle button
 *  7. Re-initialises Lucide icons after injection
 */

(async function initSidebar() {
    /* ── 1. Fetch the sidebar HTML snippet ── */
    let sidebarHTML;
    try {
        const res = await fetch('/sidebar.html');
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        sidebarHTML = await res.text();
    } catch (err) {
        console.warn('[sidebar.js] Could not load sidebar.html:', err);
        return;
    }

    /* ── 2. Inject & wrap DOM ── */
    // Wrap content that's already in <body> into .app-main > .app-content
    const existingBody = document.body.innerHTML;

    document.body.innerHTML = `
        <div class="app-layout">
            <div id="sidebar-root"></div>
            <div class="app-main">
                <div class="app-content">
                    ${existingBody}
                </div>
            </div>
        </div>
    `;

    // Insert the sidebar HTML into its root
    document.getElementById('sidebar-root').innerHTML = sidebarHTML;

    /* ── 3. Determine current page from pathname ── */
    const pathname = window.location.pathname;          // e.g. /student-dashboard.html
    const pageName = pathname.split('/').pop()          // student-dashboard.html
        .replace('.html', '');     // student-dashboard

    /* ── 4. Get current user & role ── */
    const user = (typeof getCurrentUser === 'function') ? getCurrentUser() : null;
    const role = user?.role || '';

    /* ── 5. Filter nav items by role ── */
    document.querySelectorAll('.nav-item[data-roles]').forEach(item => {
        const allowed = item.getAttribute('data-roles').split(',').map(r => r.trim());
        if (!allowed.includes(role)) {
            item.style.display = 'none';
        }
    });

    /* Filter whole sections — hide section if all items are hidden */
    document.querySelectorAll('.nav-section').forEach(section => {
        const sectionRoles = section.getAttribute('data-roles')?.split(',').map(r => r.trim()) || [];
        const visible = sectionRoles.length === 0 || sectionRoles.includes(role);
        if (!visible) {
            section.style.display = 'none';
            return;
        }
        // Also hide section label if no visible items remain
        const visibleItems = [...section.querySelectorAll('.nav-item[data-roles]')]
            .filter(el => el.style.display !== 'none');
        if (visibleItems.length === 0) {
            section.style.display = 'none';
        }
    });

    /* ── 6. Mark active nav item ── */
    document.querySelectorAll('.nav-item[data-page]').forEach(item => {
        const page = item.getAttribute('data-page');
        if (pageName === page || pageName.startsWith(page)) {
            item.classList.add('active');
        }
    });

    /* ── 7. Populate user info in footer ── */
    if (user) {
        const avatarEl = document.getElementById('sidebarUserAvatar');
        const nameEl = document.getElementById('sidebarUserName');
        const roleEl = document.getElementById('sidebarUserRole');

        if (avatarEl && typeof getInitials === 'function') {
            avatarEl.textContent = getInitials(user.fullName);
        }
        if (nameEl) nameEl.textContent = user.fullName || '';
        if (roleEl) roleEl.textContent = (user.role || '').charAt(0) + (user.role || '').slice(1).toLowerCase();
    }

    /* ── 8. Collapse toggle ── */
    const sidebar = document.getElementById('appSidebar');
    const collapseBtn = document.getElementById('sidebarCollapseBtn');

    // Restore saved state
    const isCollapsed = localStorage.getItem('sidebar-collapsed') === 'true';
    if (isCollapsed && sidebar) {
        sidebar.classList.add('collapsed');
        _updateCollapseIcon(true);
    }

    if (collapseBtn && sidebar) {
        collapseBtn.addEventListener('click', () => {
            const nowCollapsed = sidebar.classList.toggle('collapsed');
            localStorage.setItem('sidebar-collapsed', nowCollapsed);
            _updateCollapseIcon(nowCollapsed);
            if (typeof lucide !== 'undefined') lucide.createIcons();
        });
    }

    function _updateCollapseIcon(collapsed) {
        const icon = collapseBtn?.querySelector('[data-lucide]');
        if (icon) {
            icon.setAttribute('data-lucide', collapsed ? 'chevrons-right' : 'chevrons-left');
        }
    }

    /* ── 9. Logout button ── */
    const logoutBtn = document.getElementById('sidebarLogoutBtn');
    if (logoutBtn && typeof logout === 'function') {
        logoutBtn.addEventListener('click', () => logout());
    }

    /* ── 10. Re-init Lucide icons ── */
    if (typeof lucide !== 'undefined') {
        lucide.createIcons();
    } else {
        // Icons script may still be loading — wait for it
        window.addEventListener('load', () => {
            if (typeof lucide !== 'undefined') lucide.createIcons();
        });
    }

    console.log('[sidebar.js] Sidebar initialised for role:', role, '/ page:', pageName);

})();
