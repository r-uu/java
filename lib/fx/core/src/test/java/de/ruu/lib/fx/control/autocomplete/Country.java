package de.ruu.lib.fx.control.autocomplete;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.isNull;

public class Country implements Comparable<Country>
{
	private final String name;

	public Country(String name) { this.name = name; }

	public String getName() { return name; }

//	@Override public String toString() { return name(); }

	public static List<Country> countries()
	{
		List<Country> countries = new ArrayList<>();

		countries.add(new Country("Afghanistan"));
		countries.add(new Country("Albania"));
		countries.add(new Country("Algeria"));
		countries.add(new Country("Andorra"));
		countries.add(new Country("Angola"));
		countries.add(new Country("Antigua and Barbuda"));
		countries.add(new Country("Argentina"));
		countries.add(new Country("Armenia"));
		countries.add(new Country("Australia"));
		countries.add(new Country("Austria"));
		countries.add(new Country("Azerbaijan"));
		countries.add(new Country("Bahamas"));
		countries.add(new Country("Bahrain"));
		countries.add(new Country("Bangladesh"));
		countries.add(new Country("Barbados"));
		countries.add(new Country("Belarus"));
		countries.add(new Country("Belgium"));
		countries.add(new Country("Belize"));
		countries.add(new Country("Benin"));
		countries.add(new Country("Bhutan"));
		countries.add(new Country("Bolivia"));
		countries.add(new Country("Bosnia and Herzegovina"));
		countries.add(new Country("Botswana"));
		countries.add(new Country("Brazil"));
		countries.add(new Country("Brunei"));
		countries.add(new Country("Bulgaria"));
		countries.add(new Country("Burkina Faso"));
		countries.add(new Country("Burundi"));
		countries.add(new Country("Cabo Verde"));
		countries.add(new Country("Cambodia"));
		countries.add(new Country("Cameroon"));
		countries.add(new Country("Canada"));
		countries.add(new Country("Central African Republic (CAR)"));
		countries.add(new Country("Chad"));
		countries.add(new Country("Chile"));
		countries.add(new Country("China"));
		countries.add(new Country("Colombia"));
		countries.add(new Country("Comoros"));
		countries.add(new Country("Democratic Republic of the Congo"));
		countries.add(new Country("Republic of the Congo"));
		countries.add(new Country("Costa Rica"));
		countries.add(new Country("Cote d'Ivoire"));
		countries.add(new Country("Croatia"));
		countries.add(new Country("Cuba"));
		countries.add(new Country("Cyprus"));
		countries.add(new Country("Czech Republic"));
		countries.add(new Country("Denmark"));
		countries.add(new Country("Djibouti"));
		countries.add(new Country("Dominica"));
		countries.add(new Country("Dominican Republic"));
		countries.add(new Country("Ecuador"));
		countries.add(new Country("Egypt"));
		countries.add(new Country("El Salvador"));
		countries.add(new Country("Equatorial Guinea"));
		countries.add(new Country("Eritrea"));
		countries.add(new Country("Estonia"));
		countries.add(new Country("Ethiopia"));
		countries.add(new Country("Fiji"));
		countries.add(new Country("Finland"));
		countries.add(new Country("France"));
		countries.add(new Country("Gabon"));
		countries.add(new Country("Gambia"));
		countries.add(new Country("Georgia"));
		countries.add(new Country("Germany"));
		countries.add(new Country("Ghana"));
		countries.add(new Country("Greece"));
		countries.add(new Country("Grenada"));
		countries.add(new Country("Guatemala"));
		countries.add(new Country("Guinea"));
		countries.add(new Country("Guinea-Bissau"));
		countries.add(new Country("Guyana"));
		countries.add(new Country("Haiti"));
		countries.add(new Country("Honduras"));
		countries.add(new Country("Hungary"));
		countries.add(new Country("Iceland"));
		countries.add(new Country("India"));
		countries.add(new Country("Indonesia"));
		countries.add(new Country("Iran"));
		countries.add(new Country("Iraq"));
		countries.add(new Country("Ireland"));
		countries.add(new Country("Israel"));
		countries.add(new Country("Italy"));
		countries.add(new Country("Jamaica"));
		countries.add(new Country("Japan"));
		countries.add(new Country("Jordan"));
		countries.add(new Country("Kazakhstan"));
		countries.add(new Country("Kenya"));
		countries.add(new Country("Kiribati"));
		countries.add(new Country("Kosovo"));
		countries.add(new Country("Kuwait"));
		countries.add(new Country("Kyrgyzstan"));
		countries.add(new Country("Laos"));
		countries.add(new Country("Latvia"));
		countries.add(new Country("Lebanon"));
		countries.add(new Country("Lesotho"));
		countries.add(new Country("Liberia"));
		countries.add(new Country("Libya"));
		countries.add(new Country("Liechtenstein"));
		countries.add(new Country("Lithuania"));
		countries.add(new Country("Luxembourg"));
		countries.add(new Country("Macedonia (FYROM)"));
		countries.add(new Country("Madagascar"));
		countries.add(new Country("Malawi"));
		countries.add(new Country("Malaysia"));
		countries.add(new Country("Maldives"));
		countries.add(new Country("Mali"));
		countries.add(new Country("Malta"));
		countries.add(new Country("Marshall Islands"));
		countries.add(new Country("Mauritania"));
		countries.add(new Country("Mauritius"));
		countries.add(new Country("Mexico"));
		countries.add(new Country("Micronesia"));
		countries.add(new Country("Moldova"));
		countries.add(new Country("Monaco"));
		countries.add(new Country("Mongolia"));
		countries.add(new Country("Montenegro"));
		countries.add(new Country("Morocco"));
		countries.add(new Country("Mozambique"));
		countries.add(new Country("Myanmar (Burma)"));
		countries.add(new Country("Namibia"));
		countries.add(new Country("Nauru"));
		countries.add(new Country("Nepal"));
		countries.add(new Country("Netherlands"));
		countries.add(new Country("New Zealand"));
		countries.add(new Country("Nicaragua"));
		countries.add(new Country("Niger"));
		countries.add(new Country("Nigeria"));
		countries.add(new Country("North Korea"));
		countries.add(new Country("Norway"));
		countries.add(new Country("Oman"));
		countries.add(new Country("Pakistan"));
		countries.add(new Country("Palau"));
		countries.add(new Country("Palestine"));
		countries.add(new Country("Panama"));
		countries.add(new Country("Papua New Guinea"));
		countries.add(new Country("Paraguay"));
		countries.add(new Country("Peru"));
		countries.add(new Country("Philippines"));
		countries.add(new Country("Poland"));
		countries.add(new Country("Portugal"));
		countries.add(new Country("Qatar"));
		countries.add(new Country("Romania"));
		countries.add(new Country("Russia"));
		countries.add(new Country("Rwanda"));
		countries.add(new Country("Saint Kitts and Nevis"));
		countries.add(new Country("Saint Lucia"));
		countries.add(new Country("Saint Vincent and the Grenadines"));
		countries.add(new Country("Samoa"));
		countries.add(new Country("San Marino"));
		countries.add(new Country("Sao Tome and Principe"));
		countries.add(new Country("Saudi Arabia"));
		countries.add(new Country("Senegal"));
		countries.add(new Country("Serbia"));
		countries.add(new Country("Seychelles"));
		countries.add(new Country("Sierra Leone"));
		countries.add(new Country("Singapore"));
		countries.add(new Country("Slovakia"));
		countries.add(new Country("Slovenia"));
		countries.add(new Country("Solomon Islands"));
		countries.add(new Country("Somalia"));
		countries.add(new Country("South Africa"));
		countries.add(new Country("South Korea"));
		countries.add(new Country("South Sudan"));
		countries.add(new Country("Spain"));
		countries.add(new Country("Sri Lanka"));
		countries.add(new Country("Sudan"));
		countries.add(new Country("Suriname"));
		countries.add(new Country("Swaziland"));
		countries.add(new Country("Sweden"));
		countries.add(new Country("Switzerland"));
		countries.add(new Country("Syria"));
		countries.add(new Country("Taiwan"));
		countries.add(new Country("Tajikistan"));
		countries.add(new Country("Tanzania"));
		countries.add(new Country("Thailand"));
		countries.add(new Country("Timor-Leste"));
		countries.add(new Country("Togo"));
		countries.add(new Country("Tonga"));
		countries.add(new Country("Trinidad and Tobago"));
		countries.add(new Country("Tunisia"));
		countries.add(new Country("Turkey"));
		countries.add(new Country("Turkmenistan"));
		countries.add(new Country("Tuvalu"));
		countries.add(new Country("Uganda"));
		countries.add(new Country("Ukraine"));
		countries.add(new Country("United Arab Emirates (UAE)"));
		countries.add(new Country("United Kingdom (UK)"));
		countries.add(new Country("United States of America (USA)"));
		countries.add(new Country("Uruguay"));
		countries.add(new Country("Uzbekistan"));
		countries.add(new Country("Vanuatu"));
		countries.add(new Country("Vatican City (Holy See)"));
		countries.add(new Country("Venezuela"));
		countries.add(new Country("Vietnam"));
		countries.add(new Country("Yemen"));
		countries.add(new Country("Zambia"));
		countries.add(new Country("Zimbabwe"));

		return countries;
	}

	@Override public int compareTo(Country o)
	{
		if (isNull(o)) return 1;
		return name.compareToIgnoreCase(o.name);
	}
}