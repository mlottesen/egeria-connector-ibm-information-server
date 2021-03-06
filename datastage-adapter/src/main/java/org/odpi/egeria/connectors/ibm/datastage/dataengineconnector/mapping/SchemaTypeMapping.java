/* SPDX-License-Identifier: Apache-2.0 */
/* Copyright Contributors to the ODPi Egeria project. */
package org.odpi.egeria.connectors.ibm.datastage.dataengineconnector.mapping;

import org.odpi.egeria.connectors.ibm.datastage.dataengineconnector.model.DataStageCache;
import org.odpi.egeria.connectors.ibm.datastage.dataengineconnector.model.DataStageJob;
import org.odpi.egeria.connectors.ibm.igc.clientlibrary.IGCRestConstants;
import org.odpi.egeria.connectors.ibm.igc.clientlibrary.model.base.Classificationenabledgroup;
import org.odpi.egeria.connectors.ibm.igc.clientlibrary.model.base.InformationAsset;
import org.odpi.egeria.connectors.ibm.igc.clientlibrary.model.base.Link;
import org.odpi.egeria.connectors.ibm.igc.clientlibrary.model.base.Stage;
import org.odpi.egeria.connectors.ibm.igc.clientlibrary.model.common.Identity;
import org.odpi.openmetadata.accessservices.dataengine.model.SchemaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Mappings for creating a SchemaType.
 */
public class SchemaTypeMapping extends BaseMapping {

    private static final Logger log = LoggerFactory.getLogger(SchemaTypeMapping.class);
    private SchemaType schemaType;

    /**
     * Creates a SchemaType for the provided data store and field information.
     *
     * @param cache used by this mapping
     * @param storeIdentity the store identity for which to create the schema type
     */
    public SchemaTypeMapping(DataStageCache cache, Identity storeIdentity) {
        super(cache);
        schemaType = null;
        if (storeIdentity != null) {
            schemaType = new SchemaType();
            String schemaTypeQN = getFullyQualifiedName(storeIdentity, null);
            if (schemaTypeQN != null) {
                log.debug("Constructing SchemaType for data store & fields: {}", schemaTypeQN);
                schemaType.setQualifiedName(schemaTypeQN);
                schemaType.setDisplayName(storeIdentity.getName());
                InformationAsset store = new InformationAsset();
                store.setId(storeIdentity.getRid());
                store.setType(storeIdentity.getAssetType());
                store.setName(storeIdentity.getName());
                List<Classificationenabledgroup> fields = cache.getFieldsForStore(store);
                AttributeMapping attributeMapping = new AttributeMapping(cache, fields);
                schemaType.setAttributeList(attributeMapping.getAttributes());
            } else {
                log.error("Unable to determine identity of store: {}", storeIdentity);
            }
        }
    }

    /**
     * Creates a SchemaType for the provided job and link information.
     *
     * @param cache used by this mapping
     * @param job the job for which to create the SchemaType
     * @param link the link from which to retrieve stage columns for the SchemaType's attributes
     * @param fullyQualifiedStageName to ensure each attribute is unique
     */
    SchemaTypeMapping(DataStageCache cache, DataStageJob job, Link link, String fullyQualifiedStageName) {
        super(cache);
        schemaType = null;
        if (link != null) {
            schemaType = new SchemaType();
            String schemaTypeQN = getFullyQualifiedName(link, fullyQualifiedStageName);
            if (schemaTypeQN != null) {
                log.debug("Constructing SchemaType for job & link: {}", schemaTypeQN);
                schemaType.setQualifiedName(schemaTypeQN);
                schemaType.setDisplayName(link.getId());
                schemaType.setAuthor(link.getModifiedBy());
                AttributeMapping attributeMapping = new AttributeMapping(cache, job, link, fullyQualifiedStageName);
                schemaType.setAttributeList(attributeMapping.getAttributes());
            } else {
                log.error("Unable to determine identity of link: {}", link);
            }
        }
    }

    /**
     * Creates a SchemaType for the provided data store field information, for the provided stage.
     *
     * @param cache used by this mapping
     * @param stage the stage for which to create the SchemaType
     * @param storeIdentity the store identity for which to create the SchemaType
     * @param fields the fields from the data store to use in creating the SchemaType
     * @param fullyQualifiedStageName the fully-qualified name of the stage
     */
    SchemaTypeMapping(DataStageCache cache, Stage stage, Identity storeIdentity, List<Classificationenabledgroup> fields, String fullyQualifiedStageName) {
        super(cache);
        schemaType = null;
        if (stage != null) {
            schemaType = new SchemaType();
            String schemaTypeQN = getFullyQualifiedName(storeIdentity, fullyQualifiedStageName);
            if (schemaTypeQN != null) {
                log.debug("Constructing SchemaType for store & fields, within stage: {}", schemaTypeQN);
                schemaType.setQualifiedName(schemaTypeQN);
                schemaType.setDisplayName(storeIdentity.getName());
                schemaType.setAuthor((String) igcRestClient.getPropertyByName(stage, "modified_by"));
                AttributeMapping attributeMapping = new AttributeMapping(cache, fields, fullyQualifiedStageName);
                schemaType.setAttributeList(attributeMapping.getAttributes());
            } else {
                log.error("Unable to determine identity of store: {}", storeIdentity);
            }
        }
    }

    /**
     * Retrieve the SchemaType that was setup.
     *
     * @return SchemaType
     */
    public SchemaType getSchemaType() { return schemaType; }

}
