package com.churchtrack.app.ui

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.churchtrack.app.ChurchTrackApp
import com.churchtrack.app.R
import com.churchtrack.app.databinding.ActivityMainBinding
import com.churchtrack.app.service.AbsenceCheckWorker
import com.churchtrack.app.ui.auth.LoginActivity
import com.churchtrack.app.util.SessionManager
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        // Navigation setup
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_dashboard,
                R.id.nav_members,
                R.id.nav_attendance,
                R.id.nav_alerts,
                R.id.nav_finance,
                R.id.nav_projects,
                R.id.nav_users,
                R.id.nav_settings
            ),
            binding.drawerLayout
        )

        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration)
        NavigationUI.setupWithNavController(binding.navView, navController)
        NavigationUI.setupWithNavController(binding.bottomNav, navController)

        binding.navView.setNavigationItemSelectedListener(this)

        // Drawer toggle
        val toggle = ActionBarDrawerToggle(
            this, binding.drawerLayout, binding.toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Update nav header with user info
        updateNavHeader()

        // Handle navigation intent extras
        intent?.getStringExtra("navigate_to")?.let { destination ->
            if (destination == "alerts") {
                navController.navigate(R.id.nav_alerts)
            }
        }

        // Schedule absence check worker
        AbsenceCheckWorker.schedule(this)

        // Bottom nav visibility based on destination
        navController.addOnDestinationChangedListener { _, destination, _ ->
            val topLevelDestinations = setOf(
                R.id.nav_dashboard, R.id.nav_members, R.id.nav_attendance,
                R.id.nav_alerts, R.id.nav_finance, R.id.nav_projects
            )
        }

        // Show only admin-visible items
        if (!SessionManager.isAdmin(this)) {
            binding.navView.menu.findItem(R.id.nav_users)?.isVisible = false
        }
    }

    private fun updateNavHeader() {
        val headerView = binding.navView.getHeaderView(0)
        val tvName = headerView.findViewById<android.widget.TextView>(R.id.tv_nav_name)
        val tvRole = headerView.findViewById<android.widget.TextView>(R.id.tv_nav_role)
        val tvInitials = headerView.findViewById<android.widget.TextView>(R.id.tv_nav_initials)

        val fullName = SessionManager.getFullName(this)
        val role = if (SessionManager.isAdmin(this)) "Administrateur" else "Utilisateur"

        tvName?.text = fullName
        tvRole?.text = role
        tvInitials?.text = fullName.firstOrNull()?.toString() ?: "U"
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_logout -> {
                logout()
                return true
            }
            else -> NavigationUI.onNavDestinationSelected(item, navController)
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun logout() {
        SessionManager.clearSession(this)
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(navController, appBarConfiguration)
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
