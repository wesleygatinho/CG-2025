package br.edu.historiaviva.data

object HistoricalRepository {
    private val characters = listOf(
        HistoricalCharacter(
            id = "dumont",
            name = "Santos Dumont",
            role = "Inventor e Aviador",
            category = CharacterCategory.Invention,
            coverImageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuCcO67cHKB4O36IZw7_hQX2l8wCtR7VVqQVCQAWui8WcaLCziCCrN1s0XS13cAVVk1_zNTxTsFtU3T8qQ83JgVflFtFRXmplhYhsDyaijT8HuwySlRfDDUc-Qa9UrFjCEPwhMuIYJ4SEkUTEg1tuephS2-23zL4sFMDdcWgFA44qwg15prp4yBBTyh2BegzcH-zRH5S6xaZPPqv1zFGoGssItI1Jcrz_DDqSdHCBu2WLhPNZstjcGK9RMtqiX_zmg9FMcHBaXasQZ8",
            bio = listOf(
                "Alberto Santos Dumont foi um inventor e pioneiro da aviação, conhecido por seus experimentos com balões e aeronaves no início do século XX.",
                "Em Paris, projetou e pilotou diversos dirigíveis e aviões, contribuindo para popularizar a ideia de voar como tecnologia possível e útil."
            ),
            timeline = listOf(
                TimelineEvent(year = "1873", title = "Nascimento", description = "Nasce em Minas Gerais, Brasil."),
                TimelineEvent(year = "1901", title = "Dirigível nº 6", description = "Ganha o Prêmio Deutsch ao contornar a Torre Eiffel."),
                TimelineEvent(year = "1906", title = "14-Bis", description = "Realiza voo público em Paris, marco na história da aviação."),
                TimelineEvent(year = "1932", title = "Falecimento", description = "Morre no Guarujá, São Paulo.")
            ),
            funFacts = listOf(
                "Era conhecido por compartilhar ideias e projetos, incentivando avanços na aviação.",
                "Desenvolveu o relógio de pulso como solução prática para pilotos consultarem as horas em voo."
            ),
            galleryImageUrls = listOf(
                "https://lh3.googleusercontent.com/aida-public/AB6AXuAY84Z3z2YtDFCPQBdoqTuCuedlAIzchHqfXpVB4EPaa7lsh7qDvs2XfvuZhol59BvXostTs0Zo3Xl93P764IEgCkkxPIHc9OIM3kmgmi1C2Qr9mcufWZ7fsSrVLmFFDiApT4_PeEkJ40Adlxv8Uvx-OXFBEZDZvqU8MzfEVUlH5GQoaE-SOH3N8Nl6gcURQx8j4TVMJOJcXDn4A2xW3dBrBMXdkaYF4UKF6lmSyQuaDIhmkSDAT9aYWaXapWxnv9xF75dsfh8YlzE"
            ),
            modelAssetFileLocation = "models/placeholder.glb"
        ),
        HistoricalCharacter(
            id = "einstein",
            name = "Albert Einstein",
            role = "Físico Teórico",
            category = CharacterCategory.ScienceArt,
            coverImageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuDbyAMkUEUstvzBzpnELkF5l-c6SSoO86av8h07W7SFVm31bArKNeqsVo8eeVkLBAJulef4DTABvz-F4exYaVfB1X2pv6Xb6qaZ25p-IeLFX416viRHifQrOK8K0sTUsnnG4UX339JqjrUe5l7iaw1tGhZgeZGgxj_JfC3WSoVE5Z0oH-5u0-ZCNe4n1y5Fg4mxMJKnMvZriCqWxXZK8KH46_ZVqis89xgcVyFp_4VXP0eCKWFedsWg13X96tQcqHL1y7czPnlZvXQ",
            bio = listOf(
                "Albert Einstein foi um físico teórico que desenvolveu a teoria da relatividade, um dos pilares da física moderna.",
                "Recebeu o Prêmio Nobel de Física em 1921 por sua explicação do efeito fotoelétrico."
            ),
            timeline = listOf(
                TimelineEvent(year = "1879", title = "Nascimento", description = "Nasce em Ulm, Alemanha."),
                TimelineEvent(year = "1905", title = "Annus Mirabilis", description = "Publica artigos fundamentais, incluindo relatividade especial."),
                TimelineEvent(year = "1921", title = "Prêmio Nobel", description = "Recebe o Nobel pelo efeito fotoelétrico."),
                TimelineEvent(year = "1933", title = "Emigração", description = "Emigra para os Estados Unidos.")
            ),
            funFacts = listOf(
                "Recusou a presidência de Israel em 1952.",
                "Tocava violino e considerava a música essencial para seu pensamento."
            ),
            galleryImageUrls = listOf(
                "https://lh3.googleusercontent.com/aida-public/AB6AXuDrcjfancGlG09zrEzorV0Mw2c0Ava2gER-clgcih2sIWqNSNKwofYGq_z30R8c74Bbxm1rroc0m77205FiiXYX2Qf9dfiWzlSGFi9S8EXj4H_zXStmwzeejHoQJk0TccKK3v84tquw5_d5S8bZRrAu1cIXN3Cyk1zOKJboZk5tXYYbKU_xLzX9NJSmlqpRB-USPXbl8_sPqm-ZnXGMBjRBODl1Y6YXV9f9H8wzI-HqrgZJl3FTUCoGUq3RUxsluXGW45gvp33SG_U"
            ),
            modelAssetFileLocation = "models/placeholder.glb"
        ),
        HistoricalCharacter(
            id = "curie",
            name = "Marie Curie",
            role = "Cientista",
            category = CharacterCategory.ScienceArt,
            coverImageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuDEOJZEDR1B7sYx37jB7OLWij9-gIh8zrTaKcD_csljSDlmuV7qtne7AeP8M35EDAuZGRAMGBuC8rze3a_xaGDA4LV8YxK7gWWcxe78YTbr4VFa8tt5k6z1afbYptRYD3BGmOkjJcmz1GfnLkTr8B5jmUr2lYtnwXCO41v0vYUT7HPiiBaB-I8omx_cvl0NaKMoFELI0C-gnCbbbrJx41VSfvqXFcVLKR7s5S-2ERF7YvszLoTs8CpBfOzzFa2bWZogj7klJjk0wmA",
            bio = listOf(
                "Marie Curie foi uma cientista pioneira no estudo da radioatividade, área que ajudou a fundamentar.",
                "Foi a primeira pessoa a ganhar dois Prêmios Nobel em áreas diferentes (Física e Química)."
            ),
            timeline = listOf(
                TimelineEvent(year = "1867", title = "Nascimento", description = "Nasce em Varsóvia (então parte do Império Russo)."),
                TimelineEvent(year = "1898", title = "Polônio e Rádio", description = "Descobre os elementos polônio e rádio."),
                TimelineEvent(year = "1903", title = "Nobel de Física", description = "Recebe o Nobel por pesquisas sobre radiação."),
                TimelineEvent(year = "1911", title = "Nobel de Química", description = "Recebe o Nobel pela descoberta de novos elementos.")
            ),
            funFacts = listOf(
                "Seus cadernos de laboratório ainda são radioativos e precisam de cuidados especiais.",
                "Seu trabalho abriu caminho para aplicações médicas e industriais da radiação."
            ),
            galleryImageUrls = listOf(
                "https://lh3.googleusercontent.com/aida-public/AB6AXuDEOJZEDR1B7sYx37jB7OLWij9-gIh8zrTaKcD_csljSDlmuV7qtne7AeP8M35EDAuZGRAMGBuC8rze3a_xaGDA4LV8YxK7gWWcxe78YTbr4VFa8tt5k6z1afbYptRYD3BGmOkjJcmz1GfnLkTr8B5jmUr2lYtnwXCO41v0vYUT7HPiiBaB-I8omx_cvl0NaKMoFELI0C-gnCbbbrJx41VSfvqXFcVLKR7s5S-2ERF7YvszLoTs8CpBfOzzFa2bWZogj7klJjk0wmA"
            ),
            modelAssetFileLocation = "models/placeholder.glb"
        )
    )

    val allCharacters: List<HistoricalCharacter> = characters

    fun getCharacter(id: String?): HistoricalCharacter? {
        if (id.isNullOrBlank()) return null
        return characters.firstOrNull { it.id == id }
    }
}

