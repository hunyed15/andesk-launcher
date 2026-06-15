package com.andesk.launcher.ui.settings

import android.content.Context
import android.widget.ArrayAdapter
import android.widget.Spinner

/**
 * 省→市 级联数据 + adcode映射
 */
object CityData {
    data class City(val name: String, val adcode: String)

    private val data = mapOf(
        "北京市"   to listOf(City("北京", "110000")),
        "天津市"   to listOf(City("天津", "120000")),
        "上海市"   to listOf(City("上海", "310000")),
        "重庆市"   to listOf(City("重庆", "500000")),
        "河北省"   to listOf(City("石家庄","130100"),City("唐山","130200"),City("保定","130600")),
        "山西省"   to listOf(City("太原","140100"),City("大同","140200")),
        "内蒙古"   to listOf(City("呼和浩特","150100"),City("包头","150200")),
        "辽宁省"   to listOf(City("沈阳","210100"),City("大连","210200")),
        "吉林省"   to listOf(City("长春","220100"),City("吉林","220200")),
        "黑龙江省" to listOf(City("哈尔滨","230100"),City("齐齐哈尔","230200")),
        "江苏省"   to listOf(City("南京","320100"),City("苏州","320500"),City("无锡","320200"),City("常州","320400"),City("南通","320600"),City("徐州","320300"),City("扬州","321000"),City("宿迁","321300"),City("盐城","320900"),City("淮安","320800"),City("连云港","320700"),City("泰州","321200"),City("镇江","321100")),
        "浙江省"   to listOf(City("杭州","330100"),City("宁波","330200"),City("温州","330300"),City("嘉兴","330400"),City("湖州","330500"),City("绍兴","330600"),City("金华","330700"),City("台州","331000")),
        "安徽省"   to listOf(City("合肥","340100"),City("芜湖","340200"),City("蚌埠","340300")),
        "福建省"   to listOf(City("福州","350100"),City("厦门","350200"),City("泉州","350500"),City("漳州","350600")),
        "江西省"   to listOf(City("南昌","360100"),City("九江","360400"),City("赣州","360700")),
        "山东省"   to listOf(City("济南","370100"),City("青岛","370200"),City("烟台","370600"),City("潍坊","370700"),City("临沂","371300")),
        "河南省"   to listOf(City("郑州","410100"),City("洛阳","410300"),City("开封","410200"),City("南阳","411300")),
        "湖北省"   to listOf(City("武汉","420100"),City("襄阳","420600"),City("宜昌","420500")),
        "湖南省"   to listOf(City("长沙","430100"),City("株洲","430200"),City("湘潭","430300"),City("衡阳","430400")),
        "广东省"   to listOf(City("广州","440100"),City("深圳","440300"),City("东莞","441900"),City("佛山","440600"),City("珠海","440400"),City("惠州","441300"),City("中山","442000"),City("汕头","440500")),
        "广西"     to listOf(City("南宁","450100"),City("柳州","450200"),City("桂林","450300")),
        "海南省"   to listOf(City("海口","460100"),City("三亚","460200")),
        "四川省"   to listOf(City("成都","510100"),City("绵阳","510700"),City("德阳","510600"),City("宜宾","511500")),
        "贵州省"   to listOf(City("贵阳","520100"),City("遵义","520300")),
        "云南省"   to listOf(City("昆明","530100"),City("大理","532900"),City("丽江","530700")),
        "西藏"     to listOf(City("拉萨","540100")),
        "陕西省"   to listOf(City("西安","610100"),City("咸阳","610400")),
        "甘肃省"   to listOf(City("兰州","620100"),City("天水","620500")),
        "青海省"   to listOf(City("西宁","630100")),
        "宁夏"     to listOf(City("银川","640100")),
        "新疆"     to listOf(City("乌鲁木齐","650100")),
    )

    val provinces: List<String> get() = data.keys.toList()

    fun citiesFor(province: String): List<City> = data[province] ?: emptyList()

    /** 通过城市名反查 adcode */
    fun findAdcode(cityName: String): String? {
        data.values.forEach { cities ->
            cities.forEach { c ->
                if (c.name == cityName || cityName.contains(c.name) || c.name.contains(cityName)) {
                    return c.adcode
                }
            }
        }
        return null
    }

    /** 通过城市名找province+city */
    fun find(cityName: String): Pair<String, City>? {
        data.forEach { (prov, cities) ->
            cities.forEach { c ->
                if (c.name == cityName || cityName.contains(c.name) || c.name.contains(cityName)) {
                    return prov to c
                }
            }
        }
        return null
    }

    fun setupSpinners(context: Context, provinceSpinner: Spinner, citySpinner: Spinner,
                      initialCity: String, onSelected: (String, String) -> Unit) {
        val provinceAdapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, provinces)
        provinceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        provinceSpinner.adapter = provinceAdapter

        val (initProvince, _) = find(initialCity) ?: ("江苏省" to City("南京","320100"))
        provinceSpinner.setSelection(provinces.indexOf(initProvince))

        fun updateCities(province: String) {
            val cities = citiesFor(province)
            val names = cities.map { it.name }
            val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, names)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            citySpinner.adapter = adapter

            val idx = cities.indexOfFirst { it.name == initialCity }
            if (idx >= 0) citySpinner.setSelection(idx)
        }

        updateCities(initProvince)

        provinceSpinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: android.widget.AdapterView<*>?, v: android.view.View?, pos: Int, id: Long) {
                updateCities(provinces[pos])
            }
            override fun onNothingSelected(p: android.widget.AdapterView<*>?) {}
        }

        citySpinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: android.widget.AdapterView<*>?, v: android.view.View?, pos: Int, id: Long) {
                val prov = provinces[provinceSpinner.selectedItemPosition]
                val city = citiesFor(prov)[pos]
                onSelected(city.name, city.adcode)
            }
            override fun onNothingSelected(p: android.widget.AdapterView<*>?) {}
        }
    }
}
