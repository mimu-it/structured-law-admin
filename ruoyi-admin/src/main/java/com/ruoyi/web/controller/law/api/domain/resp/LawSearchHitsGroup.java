package com.ruoyi.web.controller.law.api.domain.resp;

import java.util.HashMap;
import java.util.Map;

/**
 * @author xiao.hu
 * @date 2024-01-08
 * @apiNote
 */
public class LawSearchHitsGroup {

    private Map<String, LawSearchHits> law = new HashMap<>();

    private LawSearchHits associateFile;

    public Map<String, LawSearchHits> getLaw() {
        return law;
    }

    public void putLaw(String key, LawSearchHits hits) {
        this.law.put(key, hits);
    }

    public LawSearchHits getAssociateFile() {
        return associateFile;
    }

    public void setAssociateFile(LawSearchHits associateFile) {
        this.associateFile = associateFile;
    }
}
