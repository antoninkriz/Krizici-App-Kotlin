package eu.antoninkriz.krizici

import android.os.Build
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import eu.antoninkriz.krizici.fragments.FragmentAbout
import eu.antoninkriz.krizici.fragments.FragmentContacts
import eu.antoninkriz.krizici.fragments.FragmentMain

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private var mBackPressed: Long = 0
    private var fragment: Fragment = FragmentMain()
    private val bundle: Bundle = Bundle()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Get downloaded JSON strings
        val jsonRozvrh = intent.getStringExtra("jsonRozvrh")
        val jsonContacts = intent.getStringExtra("jsonContacts")

        // Add them to the budnle so we can move this around
        bundle.putString("jsonRozvrh", jsonRozvrh)
        bundle.putString("jsonContacts", jsonContacts)

        fragment.arguments = bundle
        changeFragment(fragment)

        val drawer: DrawerLayout = findViewById(R.id.drawer_layout)
        val toggle = object : ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            override fun onDrawerClosed(drawerView: View) {
                super.onDrawerClosed(drawerView)
                if (!fragment.isAdded) {
                    changeFragment(fragment)
                }
            }
        }
        drawer.addDrawerListener(toggle)
        toggle.syncState()

        // Init navigation drawer and change its title and subtitle
        val navigationView: NavigationView = findViewById(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)

        // Set some text in navigation drawers header
        val header = navigationView.getHeaderView(0)
        val subText = header.findViewById<TextView>(R.id.navSubtitle)
        subText.text = BuildConfig.VERSION_NAME

        // Hide tabs
        val tl = findViewById<TabLayout>(R.id.tab_layout)
        tl.visibility = View.GONE
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> {
                item.isChecked = true
                fragment = FragmentMain()
                fragment.arguments = bundle
            }
            R.id.nav_contacts -> {
                item.isChecked = true
                fragment = FragmentContacts()
                fragment.arguments = bundle
            }
            R.id.nav_about -> {
                item.isChecked = true
                fragment = FragmentAbout()
            }
        }

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onBackPressed() {
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            if (mBackPressed + 2000 > System.currentTimeMillis()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    finishAndRemoveTask()
                } else {
                    finishAffinity()
                }
                return
            } else {
                Toast.makeText(baseContext, getString(R.string.doubleclicktoexit), Toast.LENGTH_SHORT).show()
            }

            mBackPressed = System.currentTimeMillis()
        }
        super.onBackPressed()
    }

    private fun changeFragment(fragment: Fragment) {
        // Don't change fragment when same fragment is already active
        if (fragment.isAdded) return

        val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.frame_container, fragment)
        transaction.disallowAddToBackStack()
        transaction.commit()
    }
}
