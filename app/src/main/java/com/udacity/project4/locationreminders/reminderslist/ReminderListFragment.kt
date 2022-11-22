package com.udacity.project4.locationreminders.reminderslist


import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import com.udacity.project4.AuthenticationActivity
import android.widget.Toast
import androidx.lifecycle.Observer
import com.firebase.ui.auth.AuthUI
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentReminderListBinding
import com.udacity.project4.utils.setup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import org.koin.androidx.viewmodel.ext.android.viewModel


class ReminderListFragment : BaseFragment() {
    private lateinit var binding: FragmentReminderListBinding
    private lateinit var auth: FirebaseAuth
    //View Model instance by using Koin :
    override val _viewModel: RemindersListViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_reminder_list,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Initializing:
        auth = Firebase.auth
        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        //Add a menu :
        addMenu()

        //Setup recycler view:
        setupRecyclerView()

        //Navigate to add reminder :
        navigateToAddReminder()

        //To refresh layout :
        refreshLayout()

        //To show loading and toast :
        showLoadingAndToast()

    }

    //Fun to add Menu :
    private fun addMenu(){
        val menuHost : MenuHost = requireActivity()

        menuHost.addMenuProvider(object  : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.main_menu,menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.logout-> {
                        //Here i will use logout fun :)
                        logout()
                        return true
                    }
                    else -> {false}
                }
            }
        },viewLifecycleOwner, Lifecycle.State.RESUMED)

    }

    //Fun will work when i press on logout , it will show alert dialog first then logout :
    private fun logout(){
        val builder = AlertDialog.Builder(requireContext())
        builder.apply {
            setTitle("Logout")
            setMessage("Are you sure?")
            setPositiveButton("Yes") { _, _ ->
                AuthUI.getInstance().signOut(requireContext()).addOnCompleteListener {
                    val intent = Intent(requireContext(),AuthenticationActivity::class.java)
                    startActivity(intent)
                }

            }
            setNegativeButton("No") { _, _ ->
            }
        }
        val dialog = builder.create()
        dialog.show()
    }

    //load the reminders list on the ui :
    override fun onResume() {
        super.onResume()
        _viewModel.loadReminders()
    }

    //Navigate to add reminder ,use the navigationCommand live data to navigate between the fragments :(will be test)
    fun navigateToAddReminder() {
        binding.addReminderFAB.setOnClickListener {
            _viewModel.navigationCommand.postValue(NavigationCommand.To(ReminderListFragmentDirections.actionReminderListFragmentToSaveReminderFragment()))
        }
    }

    //setup the recycler view using the extension function :
    private fun setupRecyclerView() {
        val adapter = RemindersListAdapter {
        }
        binding.reminderssRecyclerView.setup(adapter)
    }

    //Fun to refresh layout :
    fun refreshLayout(){
        binding.refreshLayout.setOnRefreshListener {
            _viewModel.loadReminders()
        }
    }

    //Fun to show loading and Toast in list fragment :
    fun showLoadingAndToast(){
        _viewModel.apply {
            showLoading.observe(viewLifecycleOwner, Observer {
                binding.refreshLayout.isRefreshing = it
            })
            showToast.observe(viewLifecycleOwner, Observer {
                Toast.makeText(requireContext(),it,Toast.LENGTH_LONG).show()
            })
        }
    }

}