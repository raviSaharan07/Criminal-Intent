package com.ravisaharan.criminalintent

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.ravisaharan.criminalintent.databinding.FragmentCrimeListBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID

private const val TAG= "CrimeListFragment"

class CrimeListFragment : Fragment() {
    private val crimeListViewmodel: CrimeListViewmodel by viewModels()

    private var _binding:FragmentCrimeListBinding?=null
    private val binding
        get()=checkNotNull(_binding){
            "Cannot access binding because it is null. Is the view visible?"
        }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding=FragmentCrimeListBinding.inflate(inflater,container,false)
        binding.crimeRecyclerView.layoutManager=LinearLayoutManager(context)

        //val crimes=crimeListViewmodel.crimes
        //val adapter=CrimeListAdapter(crimes)
        //binding.crimeRecyclerView.adapter=adapter


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
                crimeListViewmodel.crimes.collect {crimes->
                    if(crimes.isEmpty()){
                        binding.emptyViewTitle.setOnClickListener{
                            showNewCrime()
                        }
                    }else{
                        binding.emptyViewTitle.visibility=View.GONE
                        binding.crimeRecyclerView.adapter = CrimeListAdapter(crimes){crimeId ->
                            findNavController().navigate(CrimeListFragmentDirections.showCrimeDetail(crimeId))
                    }
                    }
                }
            }
        }

        requireActivity().addMenuProvider(object : MenuProvider{
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.fragment_crime_list,menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when(menuItem.itemId){
                    R.id.new_crime ->{
                        showNewCrime()
                        true
                    }
                    else -> false
                }
            }
        },viewLifecycleOwner)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding=null
        //binding.crimeRecyclerView.adapter=null
    }

    private fun showNewCrime(){
        viewLifecycleOwner.lifecycleScope.launch {
            val newCrime = Crime(
                id = UUID.randomUUID(),
                title= "",
                date= Date(),
                isSolved = false
            )

            crimeListViewmodel.addCrime(newCrime)
            findNavController().navigate(CrimeListFragmentDirections.showCrimeDetail(newCrime.id))
        }
    }
}