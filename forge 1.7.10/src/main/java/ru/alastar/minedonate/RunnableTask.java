package ru.alastar.minedonate;

import java.sql.Connection;

/**
 * Created by Alastar on 25.08.2017.
 */
public abstract class RunnableTask {
    public boolean m_Deleted = false;
    public abstract void run(Connection conn);

    public void Delete() {
        m_Deleted = true;
    }
}
