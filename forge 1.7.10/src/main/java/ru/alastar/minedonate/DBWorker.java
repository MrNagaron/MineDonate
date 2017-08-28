package ru.alastar.minedonate;

import ru.log_inil.mc.minedonate.localData.DataOfConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created by Alastar on 25.08.2017.
 */
public class DBWorker implements Runnable {

    private Connection[] m_Connections;
    private int m_Sleep_Time = 100;
    private ArrayList<RunnableTask> m_Tasks;
    public boolean m_Running = false;

    public DBWorker(int pool_size, DataOfConfig cfg) {
        m_Running = true;
        m_Connections = new Connection[pool_size];
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            for (int i = 0; i < pool_size; ++i) {
                m_Connections[i] = DriverManager.getConnection("jdbc:mysql://" + cfg.dbHost + ":" + cfg.dbPort + "/" + cfg.dbName, cfg.dbUser, cfg.dbPassword);

            }
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        m_Tasks = new ArrayList<RunnableTask>();
    }

    public void AddTask(RunnableTask r) {
        synchronized (m_Tasks) {
            m_Tasks.add(r);
        }
    }

    @Override
    public void run() {
        while (m_Running) {
            try {
                Thread.sleep(100);
                synchronized (m_Tasks) {
                    if(m_Tasks.size() > 0) {
                        for (int i = m_Tasks.size() - 1; i >= 0; --i) {
                            RunnableTask task = m_Tasks.get(i);
                            task.run(m_Connections[0]);
                            m_Tasks.remove(i);
                        }
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }
}
