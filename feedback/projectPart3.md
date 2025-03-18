## Project Part 3 Feedback

### Code Base

Not all features were complete. Editing moods is not fully functional, delete is not present, and the map does not show moods. Code could be better organized using folders. At the commit at the deadline code was not runnable.

### Documentation

Not all files have comments describing the purpose and role of it. User class does not have proper javadoc docs (like Event does).

### Test Cases

The tests were not runnable. The TestEvent test was in the wrong folder. It should be in the regular test folder, not AndroidTest, which is only for UI tests. You don't need to test methods like document.collection().add(), this is a firebase method and you can assume it works. You also test your getters and setters, but there is no logic in them so it's not really necessary.

### Object Oriented Design

Most relationships are missing cardinalities, and the present cardinalities are not correct. For example, each event says it needs an event adapter, but that is not true. It could be associated with 0..1 event adaptors. Also, an event adapter could have o events in it, so this cardinality should be 0..*.  Some classes, like MoodMap, are missing from the diagram.

### Backlog

Backlog was confusing as some things marked complete were not and some things that were complete were not marked as complete. If you want to go back and revisit parts of the code, I would mark the original issue as complete and make a new issue for this.

### Sprint Planning and Review

Earlier sprint plans need some improvement, but the latest ones look good. Please try to complete tasks each week instead of just at the very end.

### Demo 

Not all user stories were complete, and the code submitted at the deadline was not runnable. After some minor fixes everything that was implemented could be demonstrated. 

### Tool Use

Tool use needs to be improved. Pull requets need more descriptive titles and should have descriptions. Please assign yourself to the PR as well. Make sure you have an approved review before merging.

### General Feedback

Your team is behind schedule. Not all requirements were completed for this milestone, and there is more to do for the next milestone. Requirements that were marked as complete were not fully implemented. Please make sure that all team members are contributing. The UI looks good.
