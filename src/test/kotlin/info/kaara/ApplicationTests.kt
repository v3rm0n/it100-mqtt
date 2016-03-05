package info.kaara

import info.kaara.it100.Application
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.SpringApplicationConfiguration
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.context.web.WebAppConfiguration

@RunWith(SpringJUnit4ClassRunner::class)
@SpringApplicationConfiguration(classes = arrayOf(Application::class))
@WebAppConfiguration
@ActiveProfiles("mock")
class ApplicationTests {

    @Autowired
    lateinit var applicationContext: ConfigurableApplicationContext

    @Test
    fun contextLoads() {
        applicationContext.close()
    }

}
