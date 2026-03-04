package com.item.service.migration.category;

import com.fasterxml.jackson.core.type.TypeReference;
import com.item.util.JsonUtils;

import java.util.List;
import java.util.Map;

/**
 * @author : lh
 */
public interface Constant {
    TypeReference<Map<Integer, Integer>> TYPE_REFERENCE_MAP = new TypeReference<Map<Integer, Integer>>(){};
    TypeReference<Map<Integer, String>> TYPE_REFERENCE_MAP_I_S = new TypeReference<Map<Integer, String>>(){};
    TypeReference<List<Integer>> TYPE_REFERENCE_LIST = new TypeReference<List<Integer>>(){};
    TypeReference<List<String>> TYPE_REFERENCE_LIST_STR = new TypeReference<List<String>>(){};
    Map<String, String> CATEGORY_MAP = Map.of("Healthcare","Healthcare and Medical",
            "Creative Arts and Design","Graphic Design and Creative Services",
            "Content Creation and Influencing","Content Creation",
            "Education","Education and Training",
            "Construction and Skilled Trades","Construction and Contracting",
            "Legal Services","Legal and Law Services",
            "Community and Social Services","Social Services and Advocacy",
            "Agriculture and Environmental Services","Environmental Services and Sustainability",
            "Sports and Fitness","Sports and Recreation");

    String CATEGORY_MAP_STR = """
            {"1":14,"2":12,"3":56,"4":53,"5":57,"6":34,"7":52,"8":55,"9":8,"10":13,"11":58,"12":6,"13":49,"14":15,"15":47,"16":59,"17":29,"18":43,"19":54,"20":20,"21":9}""";

    Map<Integer, Integer> CATEGORY_IP_MAP = JsonUtils.toObject(CATEGORY_MAP_STR, TYPE_REFERENCE_MAP);

    Map<String, Integer> JOB_STATUS_MAP = Map.of("OPEN",1, "CLOSED",2, "ON_HOLD", 5, "ARCHIVED", 2);

    String PG_COMPANY_ID_NAME_MAP_STR = """
            {"1":"Item",
            "2":"Unis",
            "3":"Cubework",
            "4":"Azul Workforce",
            "5":"Unis",
            "6":"LSO",
            "7":"Plus Automation",
            "8":"Kummer",
            "9":"Mobile Ev",
            "10":"Law Divine Lux",
            "11":"DT Public Relations LLC"}""";
    Map<Integer, String > PG_COMPANY_ID_NAME_MAP = JsonUtils.toObject(PG_COMPANY_ID_NAME_MAP_STR, TYPE_REFERENCE_MAP_I_S);

    Map<String, Integer> JOB_TYPE = Map.of("FULL_TIME",1, "PART_TIME", 2, "CONTRACT",3, "TEMPORARY",4, "INTERNSHIP",5, "OTHER",6);
    Map<String, Integer> JOB_MODE = Map.of("REMOTE",2, "HYBRID",3, "ON_SITE",1, "OTHER",4);
    Map<String, Integer> JOB_SALARY_TYPE = Map.of("HOURLY",12, "DAILY",13, "WEEKLY",14, "MONTHLY",15, "YEARLY",16, "COMMISSION",17/*, "OTHER", null*/);
    Map<Integer, Integer> JOB_CURRENCY = Map.of(1,5,
            2,6,
            3,7,
            4,8,
            5,9,
            6,11,
            7,10);

    Map<Integer, String> LOCATION_TYPE_MAP = Map.of(
            1, "On-Site",
            2, "Remote",
            3, "Hybrid",
            4, "Other"
    );

    String CATEGORY_STR = """
            {
              "1": "Accounting and Financial Services",
              "2": "Advertising and Marketing",
              "3": "Agriculture and Farming",
              "4": "Automotive and Transportation",
              "5": "Beauty and Personal Care",
              "6": "Construction and Contracting",
              "7": "Consulting and Professional Services",
              "8": "Education and Training",
              "9": "Energy and Utilities",
              "10": "Entertainment and Media",
              "11": "Food and Beverage",
              "12": "Healthcare and Medical",
              "13": "Hospitality and Tourism",
              "14": "Information Technology",
              "15": "Legal and Law Services",
              "16": "Manufacturing and Industrial",
              "17": "Nonprofit and Philanthropy",
              "18": "Real Estate and Property",
              "19": "Retail and E-commerce",
              "20": "Sports and Recreation",
              "21": "Telecommunications",
              "22": "Travel and Tourism",
              "23": "Wholesale and Distribution",
              "24": "Environmental and Green Businesses",
              "25": "Fashion and Apparel",
              "26": "Home and Interior Design",
              "27": "Music and Arts",
              "28": "Photography and Videography",
              "29": "Social Services and Advocacy",
              "30": "Technology and Software",
              "31": "Web Development and Design",
              "32": "Childcare and Early Education",
              "33": "Fitness and Wellness",
              "34": "Graphic Design and Creative Services",
              "35": "Insurance and Risk Management",
              "36": "Legal and Paralegal Services",
              "37": "Manufacturing and Production",
              "38": "Professional Development and Training",
              "39": "Renewable Energy and Clean Tech",
              "40": "Security and Surveillance",
              "41": "Travel and Hospitality",
              "42": "Event Planning and Management",
              "43": "Environmental Services and Sustainability",
              "44": "Pet Services and Veterinary Care",
              "45": "Aerospace and Defense",
              "46": "Biotechnology and Pharmaceuticals",
              "47": "Government and Public Administration",
              "48": "Research and Development",
              "49": "Transportation and Logistics",
              "50": "Wedding and Event Services",
              "51": "AI Automation",
              "52": "Content Creation",
              "53": "Human Resources",
              "54": "Freelance and Contract Work",
              "55": "Engineering",
              "56": "Business and Finance",
              "57": "Marketing and Sales",
              "58": "Retail and Customer Service",
              "59": "Scientific Research"
            }""";
    Map<Integer, String> JOB_TYPE_MAP = Map.of(
            1, "Full Time",
            2, "Part Time",
            3, "Contract",
            4, "Temporary",
            5, "Internship",
            6, "Other"
    );
    Map<Integer, String> CATEGORY_TYPE_MAP = JsonUtils.toObject(CATEGORY_STR, TYPE_REFERENCE_MAP_I_S);

    Map<Integer, String> SALARY_TYPE_MAP = Map.of(
            12, "per Hour",
            13, "Daily",
            14, "Weekly",
            15, "per Month",
            16, "Yearly",
            17, "Commission"
    );

    Map<Integer, String> CURRENCY_MAP = Map.of(
            5, "USD",
            6, "GBP",
            7, "EUR",
            8, "CAD",
            9, "CNY",
            10, "INR",
            11, "PHP"
    );

    Map<String, String > STATE_MAP = Map.of("Alajuela Province",   "Alajuela");
    Map<String, String > CITY_MAP = Map.of("Bangalore Urban",   "Bengaluru Urban");
}
