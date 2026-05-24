package top.openfbi.mdnote.note.model.fe;

import lombok.Data;

import java.util.List;

@Data
public class EsNoteResult {

    private List<SimpleNote> noteList;

    private int totalRecords;
}
