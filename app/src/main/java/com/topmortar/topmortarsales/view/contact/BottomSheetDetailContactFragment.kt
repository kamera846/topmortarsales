package com.topmortar.topmortarsales.view.contact

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.topmortar.topmortarsales.databinding.FragmentBottomSheetDetailContactBinding

class BottomSheetDetailContactFragment : Fragment() {

    private lateinit var binding: FragmentBottomSheetDetailContactBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBottomSheetDetailContactBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.suratJalanOption.setOnClickListener {
            // Navigate to Surat Jalan Activity
            // Example: startActivity(Intent(context, SuratJalanActivity::class.java))
        }

        binding.invoiceOption.setOnClickListener {
            // Navigate to Invoice Activity
            // Example: startActivity(Intent(context, InvoiceActivity::class.java))
        }

        binding.reportOption.setOnClickListener {
            // Navigate to Report Activity
            // Example: startActivity(Intent(context, ReportActivity::class.java))
        }
    }
}