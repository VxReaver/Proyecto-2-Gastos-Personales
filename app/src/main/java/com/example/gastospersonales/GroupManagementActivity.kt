package com.example.gastospersonales

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment

class GroupManagementActivity : AppCompatActivity() {

    private lateinit var groupListFragment: GroupListFragment
    private lateinit var createJoinGroupFragment: CreateJoinGroupFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_management)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Finanzas Compartidas"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        groupListFragment = GroupListFragment()
        createJoinGroupFragment = CreateJoinGroupFragment()

        if (savedInstanceState == null) {
            replaceFragment(groupListFragment)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.container)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }

    fun switchToCreateJoin(isCreating: Boolean) {
        val fragment = CreateJoinGroupFragment.newInstance(isCreating)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }

    fun switchToList() {
        supportFragmentManager.popBackStack()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}

// Para que CreateJoinGroupFragment pueda acceder a GroupManagementActivity
interface GroupManagementFragment {
    fun switchToCreateJoin(isCreating: Boolean)
    fun switchToList()
}
