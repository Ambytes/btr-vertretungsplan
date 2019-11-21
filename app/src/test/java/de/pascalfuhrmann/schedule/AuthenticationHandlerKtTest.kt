import de.pascalfuhrmann.schedule.authentication.login
import de.pascalfuhrmann.schedule.authentication.logout
import de.pascalfuhrmann.schedule.scraper.parse
import de.pascalfuhrmann.schedule.scraper.sortByClass

import org.junit.Test

private const val usr = "allg1"
private const val passwd = "1234567890"

class AuthenticationHandlerKtTest {

    @Test
    fun loginSucceeds() {
        logout(usr)
        val r = login(usr, passwd)
        assert(r.first)
    }

    @Test
    fun loginSucceedsMultipleTimes() {
        for (x in 1..20) {
            logout(usr)
            val r = login(usr, passwd)

            //lets not fuck with Hr. Martini
            Thread.sleep(200)

            assert(r.first)
        }
    }

    @Test
    fun loginReceivesData() {
        logout(usr)
        val r = login(usr, passwd)
        val p = parse(r.second)
        System.out.println(sortByClass(p, "DI71"))
    }
}