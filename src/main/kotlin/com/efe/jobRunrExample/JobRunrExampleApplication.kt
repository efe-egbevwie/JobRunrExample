package com.efe.jobRunrExample

import jakarta.annotation.PostConstruct
import org.jobrunr.configuration.JobRunr
import org.jobrunr.jobs.annotations.Job
import org.jobrunr.scheduling.JobScheduler
import org.jobrunr.server.JobActivator
import org.jobrunr.storage.sql.sqlite.SqLiteStorageProvider
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Service
import org.sqlite.SQLiteDataSource
import java.time.Duration
import javax.sql.DataSource


@SpringBootApplication
class JobRunrExampleApplication {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            runApplication<JobRunrExampleApplication>(*args)
        }

    }


    @Bean
    //Returns a simple sqlite datasource
    fun dataSource(): DataSource {
        val dataSource = SQLiteDataSource()
        dataSource.url = "jdbc:sqlite:db.sqlite"
        return dataSource
    }


    @Bean
    fun jobRunrScheduler(dataSource: DataSource, jobActivator: JobActivator): JobScheduler {
        val scheduler: JobScheduler = JobRunr.configure()
            .useStorageProvider(SqLiteStorageProvider(dataSource))
            .useJobActivator(jobActivator)
            .useBackgroundJobServer()
            .useDashboard(7070)
            .initialize()
            .jobScheduler
        return scheduler
    }


}


@Service
class MyCustomRecurringJob(
    private val scheduler: JobScheduler
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    // We use a PostConstruct annotation on a function we want to run after the application starts up
    @PostConstruct
    fun scheduleMyCustomJob() {
        val jobData = "My job data"
        val jobId = "1"

        startMyJob(jobId, jobData)
    }

    /*
     Use the scheduler to schedule a recurrent job (can be other types like one-time job).
     The job logic in run inside the lambda, which  executes at most, 1 function
     */
    fun startMyJob(jobId: String, jobData: String) {
        scheduler.scheduleRecurrently(jobId, Duration.ofSeconds(10)) {
            myJob(jobData)
        }
    }

    // The actual job logic, we use the job annotation to Provide a name and configure retries in case of failures
    @Job(name = "My Custom Job", retries = 3)
    fun myJob(jobData: String) {
        logger.info("running job with custom job data -> $jobData")
    }
}



