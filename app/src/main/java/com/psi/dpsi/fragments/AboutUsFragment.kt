package com.psi.dpsi.fragments

import android.os.Bundle
import android.text.Html
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.psi.dpsi.databinding.FragmentAboutUsBinding

class AboutUsFragment : Fragment() {
    private val binding by lazy { FragmentAboutUsBinding.inflate(layoutInflater) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val aboutUsText = """
    <p>Welcome to <b>DPSI Forensic</b>, your dedicated partner on the journey from constable to PSI. Our app is designed to provide comprehensive educational resources, including free and premium courses, and a variety of study notes tailored for aspiring PSIs.</p><br>
    
    <h3>Our Mission:</h3>
    <p>At DPSI Forensic, our mission is to empower individuals in their law enforcement careers by offering high-quality educational materials and resources. We aim to support you in achieving your career goals by providing the tools and knowledge necessary for success.</p><br>
    
    <h3>What We Offer:</h3>
    <div>
        <div><b>• Free Courses:</b> Access a range of free courses designed to provide foundational knowledge and skills essential for law enforcement careers.</div>
        <div><b>• Premium Courses:</b> Unlock advanced content with our premium courses, offering in-depth knowledge and specialized training to give you a competitive edge.</div>
        <div><b>• Study Notes:</b> Purchase detailed study notes that cover key topics and concepts, helping you to efficiently prepare for exams and assessments.</div>
        <div><b>• Forensic Resources:</b> Gain insights into forensic science with our curated resources, enhancing your understanding and expertise in this crucial area of law enforcement.</div>
    </div><br>
    
    <h3>Why Choose DPSI Forensic:</h3>
    <div>
        <div><b>• Expert Content:</b> Learn from experts in the field with our carefully designed courses and materials that ensure you receive the best possible education.</div>
        <div><b>• Comprehensive Coverage:</b> Our extensive range of resources ensures you have access to all the information you need to excel in your law enforcement career.</div>
        <div><b>• Flexibility:</b> Study at your own pace and convenience with our user-friendly app, designed to fit into your busy schedule.</div>
        <div><b>• Community Support:</b> Join a supportive community of aspiring and current law enforcement professionals, sharing knowledge and experiences to help each other succeed.</div>
    </div><br>
    
    <h3>Join Us:</h3>
    <p>We invite you to explore our app and take advantage of the comprehensive educational resources we offer. Whether you are beginning your journey as a constable or advancing towards becoming a PSI, DPSI Forensic is here to support you every step of the way. Join our community and make DPSI Forensic your trusted partner in your law enforcement career.</p><br>
    
    <p>Thank you for choosing DPSI Forensic. We look forward to helping you achieve your goals and succeed in your journey from constable to PSI!</p>
""".trimIndent()

       binding.tvAboutUs.text = Html.fromHtml(aboutUsText, Html.FROM_HTML_MODE_COMPACT)
    }



}