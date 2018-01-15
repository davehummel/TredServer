package me.davehummel.tredserver.fish.waterlevel.persisted.instructions;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurerAdapter;

/**
 * Created by dmhum_000 on 2/18/2017.
 */

@Configuration
public class PumpInstructionConfiguration extends RepositoryRestConfigurerAdapter {

    @Override
    public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config) {
        config.exposeIdsFor(PumpInstruction.class);
    }

}
