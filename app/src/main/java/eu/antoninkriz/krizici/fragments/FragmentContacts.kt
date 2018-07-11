package eu.antoninkriz.krizici.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.CardView
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.eclipsesource.json.Json
import eu.antoninkriz.krizici.R
import java.util.*

class FragmentContacts : Fragment() {

    private var contacts: MutableList<Contact> = ArrayList()
    private var scale: Float = 0.toFloat()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_contacts, container, false)
        view.setLayerType(View.LAYER_TYPE_HARDWARE, null)

        val bundle = arguments
        val activity = activity

        if (bundle == null || activity == null || !bundle.containsKey("jsonContacts")) {
            Toast.makeText(context, getString(R.string.fragmentContacts_toast_error), Toast.LENGTH_LONG).show()
            return view
        }

        val tl = activity.findViewById<TabLayout>(R.id.tab_layout)
        tl.animate().scaleY(1f).setInterpolator(DecelerateInterpolator()).start()
        tl.visibility = View.GONE

        val lnl = view.findViewById<LinearLayout>(R.id.linearScrollLayout)
        scale = resources.displayMetrics.density

        val json = bundle.getString("jsonContacts")

        if (json != null) {
            try {
                Json.parse(json).asArray()[0].asArray().forEachIndexed foreach@{ index, jsonValue ->
                    val obj = jsonValue.asObject()
                    contacts.add(Contact(
                            predmety = if (obj.get("Předměty").isNull) "" else obj.get("Předměty").asString(),
                            jmeno = if (obj.get("Jmeno").isNull) "" else obj.get("Jmeno").asString(),
                            zkratka = if (obj.get("Zkratka").isNull) "" else obj.get("Zkratka").asString(),
                            telefon = if (obj.get("Telefon").isNull) "" else obj.get("Telefon").asString(),
                            email = if (obj.get("Email").isNull) "" else obj.get("Email").asString()
                    ))

                    val newView: View = addContactsCard(index) ?: return@foreach
                    lnl.addView(newView)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, getString(R.string.fragmentContacts_json_error), Toast.LENGTH_SHORT).show()
                return view
            }

        }

        return view
    }

    private fun addContactsCard(id: Int): View? {
        val dpToPx: (Int) -> Int = { inp ->
            (inp * scale + 0.5f).toInt()
        }

        if (context == null) return null
        val c = context!!

        val dp4 = dpToPx(4)
        val dp5 = dpToPx(5)

        // CardView
        val cw = CardView(c)
        var lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        lp.setMargins(dp5, dp5, dp5, if (id == contacts.size - 1) 0 else dp5)
        cw.layoutParams = lp

        // Layout inside CardWiew
        val lnin = LinearLayout(c)
        lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        lnin.layoutParams = lp
        lnin.orientation = LinearLayout.VERTICAL
        lnin.setPadding(dp5, dp5, dp5, dp5)

        // TextView layout params
        val lpm = ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

        // TextView title

        if (contacts[id].zkratka != "" && contacts[id].jmeno != "") {
            val twName = TextView(c)
            twName.textSize = 18f
            twName.text = String.format("%s - %s", contacts[id].zkratka, contacts[id].jmeno)
            twName.layoutParams = lpm
            twName.setPadding(dp4, dp4, dp4, dp4)
            lnin.addView(twName)
        } else {
            return null
        }


        // TextView subjects
        if (contacts[id].predmety != "") {
            val twPredmety = TextView(c)
            twPredmety.text = contacts[id].predmety
            twPredmety.layoutParams = lpm
            twPredmety.setPadding(dp4, dp4, dp4, dp4)
            lnin.addView(twPredmety)
        }

        // TextView email
        if (contacts[id].email != "") {
            val twEmail = TextView(c)
            val content = SpannableString(contacts[id].email)
            content.setSpan(UnderlineSpan(), 0, content.length, 0)
            twEmail.text = content
            twEmail.layoutParams = lpm
            twEmail.setPadding(dp4, dp4, dp4, dp4)
            twEmail.setTextColor(ContextCompat.getColor(c, R.color.colorPrimary))
            twEmail.setOnClickListener {
                val emailIntent = Intent(android.content.Intent.ACTION_SEND)
                emailIntent.type = "plain/text"
                emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, arrayOf(contacts[id].email))
                startActivity(Intent.createChooser(emailIntent, getString(R.string.fragmentContacts_writeMail)))
            }
            lnin.addView(twEmail)
        }

        // TextView phone
        if (contacts[id].telefon != "") {
            val twTelefon = TextView(c)
            val content = SpannableString(contacts[id].telefon)
            content.setSpan(UnderlineSpan(), 0, content.length, 0)
            twTelefon.text = content
            twTelefon.layoutParams = lpm
            twTelefon.setPadding(dp4, dp4, dp4, dp4)
            twTelefon.setTextColor(ContextCompat.getColor(c, R.color.colorPrimary))
            twTelefon.setOnClickListener {
                val intent = Intent(Intent.ACTION_DIAL)
                intent.data = Uri.parse("tel:" + contacts[id].telefon)
                startActivity(intent)
            }
            lnin.addView(twTelefon)
        }

        cw.addView(lnin)
        return cw
    }

    private class Contact(val predmety: String, val jmeno: String, val zkratka: String, val telefon: String, val email: String)
}