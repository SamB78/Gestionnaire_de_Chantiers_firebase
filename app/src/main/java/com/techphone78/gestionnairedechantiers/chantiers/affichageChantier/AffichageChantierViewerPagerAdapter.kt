package com.techphone78.gestionnairedechantiers.chantiers.affichageChantier

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class AffichageChantierViewerPagerAdapter(
    fragment: Fragment,
    private val fragments: ArrayList<Fragment>
) : FragmentStateAdapter(fragment) {

    override fun createFragment(position: Int): Fragment = fragments[position]

    override fun getItemCount(): Int {
        return fragments.size
    }
}