package com.insperity.escmobile.view.fragment


import android.os.Bundle
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import com.insperity.escmobile.R
import com.insperity.escmobile.event.OnOrgItemClicked
import com.insperity.escmobile.event.OnPunchOrgItemSelected
import com.insperity.escmobile.event.OnSelectOrgLevel
import com.insperity.escmobile.view.adapter.OrgItemsSearchAdapter
import com.insperity.escmobile.view.extension.addOnTextChanged
import kotlinx.android.synthetic.main.fragment_org_level_search.*
import kotlinx.android.synthetic.main.widget_footnote.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import javax.inject.Inject

class OrgItemsSearchFragment : BaseFragment() {

    @Inject lateinit var bus: EventBus
    @Inject lateinit var inputMethodManager: InputMethodManager

    private var adapter: OrgItemsSearchAdapter? = null
    private var orgLevelId: Long? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = inflater.inflate(R.layout.fragment_org_level_search, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        footnoteText.setText(R.string.no_org_items_text)
        clearSearchButton.setOnClickListener { searchField.setText("") }
        searchField.addOnTextChanged { onSearchFieldTextChanged(it) }
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                inputMethodManager.hideSoftInputFromWindow(searchField.windowToken, 0)
            }
        })
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        component.inject(this)
    }

    override fun onResume() {
        super.onResume()
        bus.register(this)
    }

    override fun onPause() {
        inputMethodManager.hideSoftInputFromWindow(searchField.windowToken, 0)
        bus.unregister(this)
        super.onPause()
    }

    override fun onDestroy() {
        bus.removeStickyEvent(OnSelectOrgLevel::class.java)
        super.onDestroy()
    }

    @Subscribe(sticky = true)
    fun onEvent(event: OnSelectOrgLevel) {
        orgLevelId = event.orgLevel.id
        adapter = OrgItemsSearchAdapter(bus)
        recyclerView.adapter = adapter
        adapter?.setData(event.orgItems)
        showRecyclerView(!event.orgItems.isEmpty())
    }

    @Subscribe
    fun onEvent(event: OnOrgItemClicked) {
        activity?.onBackPressed()
        orgLevelId?.let { bus.post(OnPunchOrgItemSelected(it, event.clickedPosition, searchField.text.isNotEmpty())) }
    }

    private fun onSearchFieldTextChanged(query: CharSequence) {
        clearSearchButton.visibility = if (query.toString().isEmpty()) INVISIBLE else VISIBLE
        adapter?.setFilter(query)
    }

    private fun showRecyclerView(show: Boolean) {
        recyclerContainer.visibility = if (show) VISIBLE else GONE
        footnoteText.visibility = if (show) GONE else VISIBLE
    }
}
