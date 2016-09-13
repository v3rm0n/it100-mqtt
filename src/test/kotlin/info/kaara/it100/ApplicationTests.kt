package info.kaara.it100

import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("mock")
open class ApplicationTests {

    @Autowired
    lateinit var applicationContext: ConfigurableApplicationContext

    @Test
    fun contextLoads() {
        Assert.assertTrue(applicationContext.isActive)
    }

}
