package md.abby.ndoplauncher.nodraweronepagelauncher

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import android.widget.Toast
import android.view.*
import android.view.ContextMenu.ContextMenuInfo
import kotlinx.android.synthetic.main.layout_home.*
import android.view.Menu
import android.widget.TextView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.icu.util.ULocale.getCountry
import android.text.method.TextKeyListener.clear
import java.util.*


class Home : Activity() {
    private val prefKey = "removed_set"
    private lateinit var display_apps:  List<ResolveInfo>
    private lateinit var all_apps:      List<ResolveInfo>

    private var adapterHandle: AppsAdapter = AppsAdapter()
    private lateinit var preference : SharedPreferences
    private lateinit var removedSet : MutableSet<String>


    private fun onSetWallpaper() {
        val pickWallpaper = Intent(Intent.ACTION_SET_WALLPAPER)
        val chooser = Intent.createChooser(pickWallpaper, "Choose a Wallpaper")
        startActivity(chooser)
    }

    private val clickListener = OnItemClickListener { _, _, i, _ ->
        val info = display_apps[i]
        val pkg = info.activityInfo.packageName
        val cls = info.activityInfo.name
        val component = ComponentName(pkg, cls)

        val intent = Intent()
        intent.component = component
        startActivity(intent)
    }
    override fun onBackPressed() {
        //super.onBackPressed()
    }
    private val longClickListener = AdapterView.OnItemLongClickListener { _: AdapterView<*>, view1: View, i: Int, _: Long ->
        val info = display_apps[i]
        val pkg = info.activityInfo.packageName
        //info.activityInfo.name
        val popupMenu = PopupMenu(this@Home, view1)
        popupMenu.menuInflater.inflate(R.menu.app_menu, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener{
            //val info = it.menuInfo as AdapterContextMenuInfo
            when (it.itemId) {
                R.id.menu_show_info -> {
                    val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    intent.data = Uri.parse("package:$pkg")
                    startActivity(intent)
                }
                R.id.menu_hide -> {

                    val name = display_apps[i].activityInfo.packageName
                    removedSet.add(name)
                    preference.edit().putStringSet(prefKey, removedSet).apply()

                    adapterHandle.notifyDataSetChanged()
                    apps_list.adapter = adapterHandle

                }
                R.id.menu_add_to_dock -> {
                    // edit stuff here
                    toast("add to group")
                }
                R.id.menu_uninstall -> {
                    val intent = Intent(Intent.ACTION_DELETE)
                    intent.data = Uri.parse("package:$pkg")
                    startActivity(intent)
                }
                else -> {
                    toast("else")
                }
            }
            true


        }


        popupMenu.show()
        true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_home)
        preference = PreferenceManager.getDefaultSharedPreferences(this)!!

        removedSet = if (!preference.contains(prefKey)) mutableSetOf() else {
            preference.getStringSet(prefKey, null)
        }

        loadApps()
        apps_list.adapter = adapterHandle
        apps_list.onItemClickListener = clickListener
        apps_list.onItemLongClickListener = longClickListener
        //registerForContextMenu(apps_list)
        btn2.setOnClickListener { onSetWallpaper() }

        txtSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                if (p0 != null) {
                    display_apps = if (p0.isNotEmpty()) {
                        all_apps.filter{
                            it.activityInfo.packageName.toLowerCase().contains(p0.toString().toLowerCase())
                        }
                    }else{
                        all_apps
                    }
                    adapterHandle.notifyDataSetChanged()

                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
        })
    }


    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo)
        if (v == apps_list) {
            menuInflater.inflate(R.menu.app_menu, menu)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        //menuInflater.inflate(R.menu.main, menu)
        return true
    }


    private fun loadApps() {
        val mainIntent = Intent(Intent.ACTION_MAIN, null)
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)
        ImageView(this@Home)

        all_apps = packageManager.queryIntentActivities(mainIntent, 0).sortedBy { it.activityInfo.loadLabel(packageManager).toString() }
        display_apps = all_apps.filter {
            removedSet == null ||
            removedSet.size == 0 ||
            it.activityInfo.loadLabel(packageManager).toString().indexOfAny(removedSet) != -1
        }

    }

    private fun toast(t: String){
        Toast.makeText(this, t, Toast.LENGTH_SHORT).show()
    }

    inner class AppsAdapter : BaseAdapter() {

        override fun getCount(): Int {
            return display_apps.size
        }

        override fun getItem(i: Int): Any {
            return display_apps[i]
        }

        override fun getItemId(i: Int): Long {
            return i.toLong()
        }


        override fun getView(i: Int, convertView: View?, parent: ViewGroup): View {
            val inflater = this@Home
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val gridView : View
            val info = display_apps[i]

            if (convertView == null) {

                gridView = inflater.inflate(R.layout.app_grid, parent, false)

            } else {
                gridView = convertView
            }
            val tv = gridView.findViewById<View>(R.id.app_grid_text) as TextView
            val iv = gridView.findViewById<View>(R.id.app_grid_image) as ImageView
            iv.setImageDrawable(info.activityInfo.loadIcon(packageManager))
            tv.text = info.loadLabel(packageManager)

            return gridView
        }
    }
}