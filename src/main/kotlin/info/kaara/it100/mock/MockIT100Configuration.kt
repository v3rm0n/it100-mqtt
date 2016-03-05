package info.kaara.it100.mock

import com.github.kmbulebu.dsc.it100.IT100
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile("mock")
open class MockIT100Configuration {
    @Bean
    open fun it100(): IT100 {
        return MockIT100()
    }
}
