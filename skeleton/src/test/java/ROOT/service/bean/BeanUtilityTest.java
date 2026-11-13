package {{rootPackage}}.service.bean;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BeanUtilityTest {

    @Mock
    private ApplicationContext applicationContext;

    @Test
    void shouldReturnBeanFromContext() {
        new BeanUtility().setApplicationContext(applicationContext);
        when(applicationContext.getBean(String.class)).thenReturn("bean");

        assertThat(BeanUtility.getBean(String.class)).isEqualTo("bean");
    }

    @Test
    void shouldRejectNullArguments() {
        assertThatThrownBy(() -> new BeanUtility().setApplicationContext(null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> BeanUtility.getBean(null)).isInstanceOf(IllegalArgumentException.class);
    }
}
