package com.example.data

data class SubtitleLine(
    val index: Int,
    val speaker: String,
    val sceneDesc: String,
    val english: String,
    val mongolian: String,
    val timeCode: String
)

data class VocabPreset(
    val English: String,
    val Mongolian: String,
    val pos: String,          // Part of Speech
    val explanation: String   // Short description
)

data class MovieScene(
    val id: String,
    val title: String,
    val titleMn: String,
    val genre: String,
    val level: String,
    val year: String,
    val accent: String,         // American, British, etc.
    val durationText: String,
    val cardColorHex: Long,     // Neon theme starting highlight
    val visualPrompt: String,   // Instructions for mock scene illustration
    val vocabList: List<VocabPreset>,
    val subtitles: List<SubtitleLine>
)

object MovieDataset {
    val movies = listOf(
        MovieScene(
            id = "titanic",
            title = "Titanic",
            titleMn = "Титаник",
            genre = "Драм, Мелодрам",
            level = "Beginner (Анхан шат)",
            year = "1997",
            accent = "Америк аялга",
            durationText = "03:14",
            cardColorHex = 0xFF00E5FF, // Neon Cyan
            visualPrompt = "Titanic famous bow scene with Rose and Jack",
            vocabList = listOf(
                VocabPreset("trust", "итгэх", "Verb", "Надад итгэ гэж хэлэхэд ашиглана."),
                VocabPreset("beautiful", "үзэсгэлэнтэй, сайхан", "Adjective", "Маш гоо үзэсгэлэнтэй зүйлийг тайлбарлахад."),
                VocabPreset("flying", "нисэж буй", "Adjective", "Агаарт нисэх хөдөлгөөн."),
                VocabPreset("eyes", "нүднүүд", "Noun", "Харах эрхтэн."),
                VocabPreset("open", "нээх", "Verb", "Ямар нэгэн зүйлийг задлах, нээх.")
            ),
            subtitles = listOf(
                SubtitleLine(0, "Jack", "Жэк Рөүзийн нүдийг аниулаад хөлгийн урд зогсоож байна", "Close your eyes. No peeking.", "Нүдээ аниарай. Хялайж харж болохгүй шүү.", "00:03"),
                SubtitleLine(1, "Rose", "Рөүс Жэкийн гарыг чанга атган догдолж байна", "I'm not peeking!", "Би хялайж хараагүй ээ!", "00:06"),
                SubtitleLine(2, "Jack", "Рөүзийг хөлгийн ирмэг рүү зөөлөн хөтлөнө", "Step up onto the rail. Hold on. Trust me.", "Хашлага дээр гишгээрэй. Чанга бариарай. Надад итгэ.", "00:10"),
                SubtitleLine(3, "Rose", "Нүдээ аниастай гараа дэлгэн тэнцвэрээ олно", "I trust you.", "Би чамд итгэж байна.", "00:14"),
                SubtitleLine(4, "Jack", "Рөүзийн араас түшиж гараас нь зөөлөн атгана", "Alright, open your eyes.", "За, одоо нүдээ нээгээрэй.", "00:18"),
                SubtitleLine(5, "Rose", "Нүдээ нээгээд уудам далайг харан уулга алдана", "I'm flying, Jack! It's so beautiful!", "Би нисэж байна, Жэк! Ямар үзэсгэлэнтэй юм бэ!", "00:22"),
                SubtitleLine(6, "Jack", "Рөүзийн хажууд баярлан зөөлөн шивнэнэ", "Everything is yours, Rose. Right here.", "Энэ бүхэн чинийх, Рөүс. Яг энд.", "00:27")
            )
        ),
        MovieScene(
            id = "inception",
            title = "Inception",
            titleMn = "Эхлэл",
            genre = "Зөгнөлт, Тулаант",
            level = "Advanced (Ахисан шат)",
            year = "2010",
            accent = "Америк аялга",
            durationText = "02:28",
            cardColorHex = 0xFFF51B62, // Neon Magenta
            visualPrompt = "Cobb explaining dream architecture with folding streets",
            vocabList = listOf(
                VocabPreset("architecture", "урлаг, уран барилга", "Noun", "Зүүдний орчин төлөвлөх бүтэц."),
                VocabPreset("subconscious", "далд ухамсар", "Noun", "Бидний мэдэлгүй ажилладаг тархины хэсэг."),
                VocabPreset("projection", "төсөөлөл, тусгал", "Noun", "Зүүдэнд бий болох далд ухамсарын хүмүүс."),
                VocabPreset("collapse", "нурах, сүйрэх", "Verb", "Зүүдний ертөнц унах, устгах."),
                VocabPreset("secrets", "нууцууд", "Noun", "Бусдаас нууцалсан чухал мэдээллүүд.")
            ),
            subtitles = listOf(
                SubtitleLine(0, "Cobb", "Кобб Кофены газарт Ариаднед зүүдний бүтцийг тайлбарлаж байна", "You create the world of the dream.", "Чи зүүдний ертөнцийг өөрөө бүтээдэг амьд барилгачин юм.", "00:04"),
                SubtitleLine(1, "Ariadne", "Ариадне гайхан кофены аягаа барина", "But who populates it?", "Харин тэр зүүдэнд хэн амьдардаг юм бэ?", "00:08"),
                SubtitleLine(2, "Cobb", "Холбоотой сэдвийг нухацтай хэлнэ", "Our subconscious populates it with projections.", "Манай далд ухамсар төсөөллүүдээр түүнийг дүүргэдэг.", "00:12"),
                SubtitleLine(3, "Cobb", "Кобб ширээн дээрх цаасыг нугалж байна", "We bring the subject into that dream, and they fill it with secrets.", "Бид зорилтот хүнээ тэр зүүд рүү урьж, тэд нууцаараа дүүргэдэг.", "00:18"),
                SubtitleLine(4, "Ariadne", "Орчин тойрон улам сонин болоход гайхна", "It feels so real while we're in it.", "Зүүдэн дотор байхад бүх зүйл бодит мэт санагддаг.", "00:23"),
                SubtitleLine(5, "Cobb", "Чухал санааг анхааруулна", "Exactly. It's only when we wake up that we realize something was strange.", "Яг тийм. Сэрэх үедээ л ямар нэг зүйл хачин байсныг анзаардаг.", "00:29")
            )
        ),
        MovieScene(
            id = "wednesday",
            title = "Wednesday",
            titleMn = "Лхагва гараг",
            genre = "Ид шид, Нууцлаг",
            level = "Intermediate (Дунд шат)",
            year = "2022",
            accent = "Британи аялга",
            durationText = "01:45",
            cardColorHex = 0xFFFFE500, // Neon Yellow
            visualPrompt = "Wednesday Addams with cell phone sarcasm and her companion Thing",
            vocabList = listOf(
                VocabPreset("soul-sucking", "сүнс сорсон, ядраасан", "Adjective", "Сэтгэл санааг хомслох муу зүйл."),
                VocabPreset("void", "хоосон орон зай, ангал", "Noun", "Юу ч байхгүй чимээгүй орчин."),
                VocabPreset("weakness", "сул тал, сул дорой байдал", "Noun", "Хүч чадалгүй байх байдал."),
                VocabPreset("obsession", "донтолт, дотносолт", "Noun", "Хэтэрхий их донтох зүйл."),
                VocabPreset("emotions", "сэтгэл хөдлөлүүд", "Noun", "Уурлах, баярлах зэрэг дотоод мэдрэмж.")
            ),
            subtitles = listOf(
                SubtitleLine(0, "Enid", "Энид өөрийн гар утсыг сонирхуулан догдлон ярина", "You need to get a social media account.", "Чи сошиал медиа хаяг одоо тээж нээх хэрэгтэй байна.", "00:03"),
                SubtitleLine(1, "Wednesday", "Вэйнсдэй өөрийн бичгийн машинаа ширтэн маш нухацтай хариулна", "I find social media to be a soul-sucking void of meaningless affirmation.", "Би сошиал хуудсуудыг үнэ цэнгүй магтаалын сүнс сорогч хоосон орон зай гэж үздэг.", "00:09"),
                SubtitleLine(2, "Enid", "Энид нүдээ эргэлдүүлэн өхөөрдөм дургүйцнэ", "You are so dramatic, even early in the morning.", "Чи өглөө эрт ч гэсэн аймаар жүжигтэй байх юм аа.", "00:14"),
                SubtitleLine(3, "Wednesday", "Ширээний хажуугаас харан шивнэнэ", "I don't care about being liked or followed.", "Надад бусдад таалагдах юм уу, дагагчтай болох ямар ч хамаагүй.", "00:19"),
                SubtitleLine(4, "Wednesday", "Энид рүү хандан хүйтнээр харна", "Social media feeds on digital obsession. It's a clear sign of weakness.", "Сошиал медиа нь дижитал донтолтыг тэжээдэг. Энэ бол сул дорой байдлын тодорхой шинж.", "00:25")
            )
        ),
        MovieScene(
            id = "lionking",
            title = "The Lion King",
            titleMn = "Хаан Арслан",
            genre = "Анимаци, Гэр бүл",
            level = "Beginner (Анхан шат)",
            year = "1994",
            accent = "Америк аялга",
            durationText = "01:22",
            cardColorHex = 0xFF22FF66, // Neon Green
            visualPrompt = "Mufasa showing Simba the kingdom horizon during sunrise",
            vocabList = listOf(
                VocabPreset("kingdom", "хаант улс, эзэнт гүрэн", "Noun", "Хааны удирдлага доорх газар нутаг."),
                VocabPreset("shadowy", "сүүдэртэй, харанхуй", "Adjective", "Нарны гэрэл тусаагүй харанхуй орчин."),
                VocabPreset("horizon", "тэнгэрийн хаяа", "Noun", "Газар тэнгэр нийлж буй шугам."),
                VocabPreset("respect", "хүндэтгэх", "Verb", "Бусдын орон зайг үнэлэх, хүндлэх."),
                VocabPreset("delicate", "эмзэг, нарийн нандин", "Adjective", "Амархан эвдрэх, нарийн тэнцвэртэй байдал.")
            ),
            subtitles = listOf(
                SubtitleLine(0, "Mufasa", "Муфаса бяцхан Симбатай өндөр цохион дээрээс нар мандахыг харж зогсоно", "Look, Simba. Everything the light touches is our kingdom.", "Хараач, Симба. Нарны гэрэл тусаж буй бүхий л газар бидний вант улс юм.", "00:04"),
                SubtitleLine(1, "Simba", "Симба тэнгэрийн хаяа руу гайхан заана", "Wow... A king's realm is so huge.", "Пөөх... Хааны газар нутаг ямар том юм бэ.", "00:09"),
                SubtitleLine(2, "Simba", "Хол сүүдэртэй хөндийг заан Муфасагаас асууна", "But what about that shadowy place over there?", "Харин тэр тэнд харагдаж байгаа сүүдэртэй харанхуй газар юу вэ?", "00:13"),
                SubtitleLine(3, "Mufasa", "Симбагийн өмнө суун маш нухацтай анхааруулна", "That is beyond our borders. You must never go there, Simba.", "Энэ бол манай хил хязгаараас чанагш газар. Тийшээ хэзээ ч очиж болохгүй, Симба.", "00:19"),
                SubtitleLine(4, "Simba", "Хаан хүн өөрийнхөөрөө байх тухай бодсоноо хэлнэ", "But I thought a king can do whatever he wants.", "Хаан хүн дуртай зүйлээ хийж болдог гэж би бодсон шүү дээ.", "00:23"),
                SubtitleLine(5, "Mufasa", "Муфаса толгой сорж, зөөлөн хэлнэ", "There is more to being a king than getting your way.", "Хаан байна гэдэг чинь зөвхөн өөрийн дураар байна гэсэн үг биш юм.", "00:28"),
                SubtitleLine(6, "Mufasa", "Нарны өөдөөс харж зөвлөнө", "We must respect all creatures, from the crawling ant to the leaping antelope.", "Бид мөлхөж буй шоргоолжноос авахуулаад дүүлж буй бөөрөнхий гөрөөс хүртэлх бүх амьтдыг хүндэтгэх ёстой.", "00:34")
            )
        )
    )
}
