package se.inera.certificate.mc2wc.web.control;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import se.inera.certificate.mc2wc.batch.listener.CertificateMigrationListener;

/**
 *
 */
@Controller
public class IndexController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private CertificateMigrationListener migrationListener;

    @Autowired
    @Qualifier("migrationJob")
    private Job migrationJob;

    private JobExecution execution;

    @RequestMapping("/home")
    public String home(){
        return "home";
    }


    @RequestMapping("/startMigration")
    public String runMigrationJob() throws Exception {

        JobParameters params = new JobParameters();

        execution = jobLauncher.run(migrationJob, params);

        return "forward:/d/checkMigration";
    }

    @RequestMapping("/checkMigration")
    public String checkMigrationJob(Model model) throws Exception {

        model.addAttribute("status", execution.getStatus());

        model.addAttribute("readCount", migrationListener.getReadCount());
        model.addAttribute("readError", migrationListener.getReadError());
        model.addAttribute("skipCount", migrationListener.getSkipCount());
        model.addAttribute("writeCount", migrationListener.getWriteCount());
        model.addAttribute("writeError", migrationListener.getWriteError());

        if(execution.isRunning()){
            return "checkMigration";
        }
        return "forward:/d/migrationFinished";
    }

    @RequestMapping("/migrationFinished")
    public String migrationFinished() throws Exception {

        switch (execution.getStatus()){
            case COMPLETED:

                break;
            case FAILED:
                break;
            default:
                logger.error("Unknown status: {}", execution.getStatus());
        }

        execution = null;

        return "migrationSummary";
    }







}
