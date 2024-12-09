class MainActivity : AppCompatActivity() {
    // Do not change the line below!
    lateinit var searchBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        searchBtn = findViewById(R.id.search_btn)
        searchBtn.setOnClickListener {
            performSearch()
        }

    }
}