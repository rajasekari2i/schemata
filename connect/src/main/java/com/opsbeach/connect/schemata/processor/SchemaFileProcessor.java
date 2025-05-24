package com.opsbeach.connect.schemata.processor;

import java.util.List;

import com.opsbeach.connect.github.entity.ClientRepo;
import com.opsbeach.connect.schemata.entity.Table;

public interface SchemaFileProcessor {

    void parseFolder(String path, ClientRepo clientRepo);
    
    List<Table> getTables(String path, Boolean toSave) throws Exception;

    List<Table> getTables(byte[] path, Boolean toSave) throws Exception;

    String getFileContent(Table table);
}
