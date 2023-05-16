/*=========================================================================

    DeDup © Pan American Health Organization, 2018.
    See License at: https://github.com/bireme/DeDup/blob/master/LICENSE.txt

  ==========================================================================*/

package br.bireme.ddp;

import java.util.Set;
import jakarta.ws.rs.core.Application;

/**
 *
 * @author Heitor Barbieri
 * date: 20150928
 */
@jakarta.ws.rs.ApplicationPath("/services")
public class ApplicationConfig extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> resources = new java.util.HashSet<Class<?>>();
        addRestResourceClasses(resources);
        return resources;
    }

    /**
     * Do not modify addRestResourceClasses() method.
     * It is automatically populated with
     * all resources defined in the project.
     * If required, comment out calling this method in getClasses().
     */
    private void addRestResourceClasses(Set<Class<?>> resources) {
        resources.add(br.bireme.ddp.DeDup.class);
    }
}
