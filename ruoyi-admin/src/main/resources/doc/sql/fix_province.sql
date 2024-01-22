SELECT authority, authority_province, authority_city FROM structured_law.sl_law where authority_province="";

update structured_law.sl_law set authority_province = '全国性机关'
       where authority in ("全国人民代表大会", "全国人民代表大会常务委员会",
                           "中国人民政治协商会议", "中央人民政府委员会", "国务院", "国家监察委员会", "最高人民法院",
                           "最高人民法院、最高人民检察院", "最高人民检察院");

update structured_law.sl_law set authority_province = '四川省' where authority_city like "%甘孜%";
update structured_law.sl_law set authority_province = '云南省' where authority_city like "%大理%";
update structured_law.sl_law set authority_province = '青海省' where authority_city = "海南藏族自治州市";
update structured_law.sl_law set authority_province = '甘肃省' where authority_city = "甘南藏族自治州市";
update structured_law.sl_law set authority_province = '云南省' where authority_city = "德宏傣族景颇族自治州市";
update structured_law.sl_law set authority_province = '四川省' where authority_city = "凉山彝族自治州市";
update structured_law.sl_law set authority_province = '云南省' where authority_city = "西双版纳傣族自治州市";
update structured_law.sl_law set authority_province = '西藏自治区' where authority_city = "日喀则地区市";
update structured_law.sl_law set authority_province = '青海省' where authority_city = "海北藏族自治州市";
update structured_law.sl_law set authority_province = '湖北省' where authority_city = "恩施土家族苗族自治州市";
update structured_law.sl_law set authority_province = '云南省' where authority_city = "迪庆藏族自治州市";
update structured_law.sl_law set authority_province = '新疆维吾尔自治区' where authority_city = "昌吉回族自治州市";
update structured_law.sl_law set authority_province = '新疆维吾尔自治区' where authority_city = "哈密地区市";
update structured_law.sl_law set authority_province = '新疆维吾尔自治区' where authority_city = "伊犁哈萨克自治州市";
update structured_law.sl_law set authority_province = '贵州省' where authority_city = "黔南布依族苗族自治州市";


